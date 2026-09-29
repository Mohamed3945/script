package st.tt.script_back.services;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.entities.ComputationFormula;
import st.tt.script_back.entities.FormulaReference;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.entities.StepParameter;
import st.tt.script_back.enums.ActivationState;
import st.tt.script_back.enums.ComputationStatus;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.RoundingPolicy;
import st.tt.script_back.repositories.ComputationFormulaRepository;
import st.tt.script_back.repositories.RecipeRepository;
import st.tt.script_back.repositories.StepParameterRepository;

/**
 * Évalue les formules numériques et met à jour les paramètres calculés.
 *
 * <p>Les formules sont triées selon leurs dépendances avant évaluation. Les
 * erreurs fonctionnelles sont reportées dans {@link ComputationStatus} plutôt
 * que propagées comme erreurs techniques vers l'appelant.
 */
@Service
public class ComputationEvaluationService {

    private static final MathContext DIVISION_CONTEXT = new MathContext(20, RoundingMode.HALF_UP);

    private final RecipeRepository recipeRepository;
    private final ComputationFormulaRepository computationFormulaRepository;
    private final StepParameterRepository stepParameterRepository;

    public ComputationEvaluationService(
            RecipeRepository recipeRepository,
            ComputationFormulaRepository computationFormulaRepository,
            StepParameterRepository stepParameterRepository) {
        this.recipeRepository = recipeRepository;
        this.computationFormulaRepository = computationFormulaRepository;
        this.stepParameterRepository = stepParameterRepository;
    }

    @Transactional
    /**
     * Recalcule tous les paramètres calculés d'une recette.
     *
     * <p>Les formules sont lues depuis la recette golden associée. Les références
     * sont adressées par code d'étape et chemin de définition, puis les résultats
     * sont arrondis selon la politique de chaque formule.
     *
     * <p>Un identifiant nul, une recette sans formule ou une recette sans paramètre
     * entraîne une absence d'opération. Les paramètres invalides sont marqués avec
     * un statut de calcul adapté et leur valeur est supprimée.
     *
     * @param recipeId identifiant de la recette à recalculer
     * @throws EntityNotFoundException si la recette n'existe pas
     */
    public void recomputeRecipeComputedParameters(Long recipeId) {
        if (recipeId == null) {
            return;
        }

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));

        Long goldenRecipeId = resolveGoldenRecipeId(recipe);
        List<ComputationFormula> formulas = computationFormulaRepository.findByRecipeIdWithReferences(goldenRecipeId);

        if (formulas.isEmpty()) {
            return;
        }

        List<StepParameter> parameters = stepParameterRepository.findByRecipeIdWithStepAndDefinition(recipeId);
        if (parameters.isEmpty()) {
            return;
        }

        Map<String, StepParameter> parameterByAddress = new HashMap<>();
        for (StepParameter parameter : parameters) {
            String address = buildAddress(parameter);
            if (address != null) {
                parameterByAddress.put(address, parameter);
            }
        }

        List<ComputationFormula> orderedFormulas = sortFormulasTopologically(formulas);
        Instant now = Instant.now();

        for (ComputationFormula formula : orderedFormulas) {
            applyFormula(formula, parameterByAddress, now);
        }

        stepParameterRepository.saveAll(parameters);
    }

    private void applyFormula(ComputationFormula formula, Map<String, StepParameter> parameterByAddress, Instant now) {
        String targetAddress = toAddress(formula.getTargetStepCode(), formula.getTargetDefinitionPath());
        StepParameter target = targetAddress == null ? null : parameterByAddress.get(targetAddress);

        if (target == null) {
            return;
        }

        Map<Integer, BigDecimal> slotValues = new HashMap<>();

        for (FormulaReference reference : safeReferences(formula)) {
            String sourceAddress = toAddress(reference.getStepCode(), reference.getDefinitionPath());
            StepParameter source = sourceAddress == null ? null : parameterByAddress.get(sourceAddress);

            if (source == null) {
                markFailure(target, ComputationStatus.MISSING_INPUT, now);
                return;
            }

            if (source.getActivationState() == ActivationState.DISABLED) {
                markFailure(target, ComputationStatus.INACTIVE_INPUT, now);
                return;
            }

            BigDecimal numericValue = parseNumericJson(source.getValueJson());
            if (numericValue == null) {
                markFailure(target, ComputationStatus.MISSING_INPUT, now);
                return;
            }

            slotValues.put(reference.getSlot(), numericValue);
        }

        try {
            BigDecimal rawResult = evaluateExpression(formula.getExpression(), slotValues);
            BigDecimal rounded = applyRounding(rawResult, formula.getRoundingMode(), formula.getDecimals());
            target.setValueJson(rounded.stripTrailingZeros().toPlainString());
            target.setComputationStatus(ComputationStatus.OK);
            target.setComputedAt(now);
            target.setUserModified(false);
        } catch (ArithmeticException ex) {
            markFailure(target, ComputationStatus.DIV_ZERO, now);
        } catch (IllegalArgumentException ex) {
            markFailure(target, ComputationStatus.NOT_EVALUATED, now);
        }
    }

    private void markFailure(StepParameter target, ComputationStatus status, Instant now) {
        target.setValueJson(null);
        target.setComputationStatus(status);
        target.setComputedAt(now);
        target.setUserModified(false);
    }

    private BigDecimal parseNumericJson(String valueJson) {
        if (valueJson == null || valueJson.isBlank()) {
            return null;
        }

        String raw = valueJson.trim();
        if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length() >= 2) {
            raw = raw.substring(1, raw.length() - 1).trim();
        }

        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BigDecimal evaluateExpression(String expression, Map<Integer, BigDecimal> slotValues) {
        List<String> tokens = tokenize(expression);
        List<String> rpn = toRpn(tokens);
        return evalRpn(rpn, slotValues);
    }

    private List<String> tokenize(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("expression is empty");
        }

        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < expression.length()) {
            char c = expression.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (c == '{') {
                int end = expression.indexOf('}', i + 1);
                if (end <= i + 1) {
                    throw new IllegalArgumentException("invalid slot token");
                }
                String slot = expression.substring(i + 1, end).trim();
                if (!slot.matches("\\d+")) {
                    throw new IllegalArgumentException("slot must be numeric");
                }
                tokens.add("SLOT:" + slot);
                i = end + 1;
                continue;
            }

            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')') {
                tokens.add(String.valueOf(c));
                i++;
                continue;
            }

            if (Character.isDigit(c) || c == '.') {
                int start = i;
                i++;
                while (i < expression.length()) {
                    char n = expression.charAt(i);
                    if (Character.isDigit(n) || n == '.') {
                        i++;
                    } else {
                        break;
                    }
                }
                tokens.add("NUM:" + expression.substring(start, i));
                continue;
            }

            if( Character.isLetter(c) ) {
                int start = i;
                i++;
                while (i < expression.length() && Character.isLetterOrDigit(expression.charAt(i))) {
                    i++;
                }

                String word = expression.substring(start, i);
                if ("ABS".equalsIgnoreCase(word)) {
                    tokens.add("FUNC:ABS");
                    continue;
                }
                throw new IllegalArgumentException("Unsupported function: " + word);
            }

            throw new IllegalArgumentException("Unsupported token in expression");
        }

        return tokens;
    }

    private List<String> toRpn(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Deque<String> operators = new ArrayDeque<>();

        for (String token : tokens) {
            if (token.startsWith("SLOT:") || token.startsWith("NUM:")) {
                output.add(token);
                continue;
            }

            if (token.startsWith("FUNC:")) {
                operators.push(token);
                continue;
            }

            if ("(".equals(token)) {
                operators.push(token);
                continue;
            }

            if (")".equals(token)) {
                while (!operators.isEmpty() && !"(".equals(operators.peek())) {
                    output.add(operators.pop());
                }
                if (operators.isEmpty() || !"(".equals(operators.peek())) {
                    throw new IllegalArgumentException("Mismatched parentheses");
                }
                operators.pop();

                if (!operators.isEmpty() && operators.peek().startsWith("FUNC:")) {
                    output.add(operators.pop());
                }

                continue;
            }

            while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(token)) {
                if ("(".equals(operators.peek())) {
                    break;
                }
                output.add(operators.pop());
            }
            operators.push(token);
        }

        while (!operators.isEmpty()) {
            String op = operators.pop();
            if ("(".equals(op) || ")".equals(op)) {
                throw new IllegalArgumentException("Mismatched parentheses");
            }
            output.add(op);
        }

        return output;
    }

    private BigDecimal evalRpn(List<String> rpn, Map<Integer, BigDecimal> slotValues) {
        Deque<BigDecimal> stack = new ArrayDeque<>();

        for (String token : rpn) {
            if (token.startsWith("NUM:")) {
                stack.push(new BigDecimal(token.substring(4)));
                continue;
            }

            if (token.startsWith("SLOT:")) {
                Integer slot = Integer.parseInt(token.substring(5));
                BigDecimal value = slotValues.get(slot);
                if (value == null) {
                    throw new IllegalArgumentException("Missing slot value " + slot);
                }
                stack.push(value);
                continue;
            }

            if ("FUNC:ABS".equals(token)) {
                if (stack.isEmpty()) {
                    throw new IllegalArgumentException("Invalid expression");
                }

                BigDecimal value = stack.pop();
                stack.push(value.abs());
                continue;
            }

            if (stack.size() < 2) {
                throw new IllegalArgumentException("Invalid expression");
            }

            BigDecimal right = stack.pop();
            BigDecimal left = stack.pop();

            BigDecimal result = switch (token) {
                case "+" -> left.add(right);
                case "-" -> left.subtract(right);
                case "*" -> left.multiply(right);
                case "/" -> {
                    if (right.compareTo(BigDecimal.ZERO) == 0) {
                        throw new ArithmeticException("Division by zero");
                    }
                    yield left.divide(right, DIVISION_CONTEXT);
                }
                default -> throw new IllegalArgumentException("Unsupported operator");
            };

            stack.push(result);
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid expression");
        }

        return stack.pop();
    }

    private int precedence(String operator) {
        return switch (operator) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            default -> 0;
        };
    }

    private BigDecimal applyRounding(BigDecimal value, RoundingPolicy policy, Integer decimals) {
        if (value == null) {
            return null;
        }
        if (decimals == null) {
            return value;
        }
        if (decimals < 0) {
            throw new IllegalArgumentException("decimals must be >= 0");
        }

        RoundingMode roundingMode = switch (policy == null ? RoundingPolicy.HALF_UP : policy) {
            case NONE -> null;
            case HALF_UP -> RoundingMode.HALF_UP;
            case HALF_EVEN -> RoundingMode.HALF_EVEN;
            case FLOOR -> RoundingMode.FLOOR;
            case CEIL -> RoundingMode.CEILING;
        };

        if (roundingMode == null) {
            return value;
        }
        return value.setScale(decimals, roundingMode);
    }

    private List<ComputationFormula> sortFormulasTopologically(List<ComputationFormula> formulas) {
        Map<String, ComputationFormula> formulaByTarget = new HashMap<>();
        for (ComputationFormula formula : formulas) {
            String target = toAddress(formula.getTargetStepCode(), formula.getTargetDefinitionPath());
            if (target != null) {
                formulaByTarget.put(target, formula);
            }
        }

        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, Set<String>> outgoing = new HashMap<>();

        for (String target : formulaByTarget.keySet()) {
            inDegree.put(target, 0);
            outgoing.put(target, new HashSet<>());
        }

        for (ComputationFormula formula : formulas) {
            String target = toAddress(formula.getTargetStepCode(), formula.getTargetDefinitionPath());
            if (target == null) {
                continue;
            }
            for (FormulaReference reference : safeReferences(formula)) {
                String source = toAddress(reference.getStepCode(), reference.getDefinitionPath());
                if (source != null && formulaByTarget.containsKey(source) && outgoing.get(source).add(target)) {
                    inDegree.put(target, inDegree.getOrDefault(target, 0) + 1);
                }
            }
        }

        Deque<String> queue = new ArrayDeque<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<ComputationFormula> ordered = new ArrayList<>();
        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            ordered.add(formulaByTarget.get(current));

            for (String next : outgoing.getOrDefault(current, Set.of())) {
                int degree = inDegree.get(next) - 1;
                inDegree.put(next, degree);
                if (degree == 0) {
                    queue.addLast(next);
                }
            }
        }

        if (ordered.size() != formulaByTarget.size()) {
            return formulas.stream()
                    .sorted(Comparator.comparing(ComputationFormula::getId, Comparator.nullsLast(Long::compareTo)))
                    .toList();
        }

        return ordered;
    }

    private Long resolveGoldenRecipeId(Recipe recipe) {
        Recipe current = recipe;
        Set<Long> visited = new HashSet<>();

        while (current != null && current.getRecipeKind() != RecipeKind.GOLDEN) {
            if (current.getId() != null && !visited.add(current.getId())) {
                throw new IllegalStateException("Cycle detected while resolving golden recipe");
            }

            Long parentId = current.getParentRecipe() != null ? current.getParentRecipe().getId() : null;
            if (parentId == null) {
                break;
            }

            current = recipeRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent recipe with id " + parentId + " not found"));
        }

        return current != null && current.getId() != null ? current.getId() : recipe.getId();
    }

    private String buildAddress(StepParameter parameter) {
        if (parameter == null || parameter.getStep() == null || parameter.getStep().getCode() == null) {
            return null;
        }

        List<String> segments = new ArrayList<>();
        StepParameter cursor = parameter;
        int guard = 0;
        while (cursor != null) {
            if (guard++ > 256) {
                throw new IllegalStateException("Invalid parent chain depth for parameter " + parameter.getId());
            }

            Long definitionId = cursor.getDefinition() != null ? cursor.getDefinition().getId() : null;
            if (definitionId == null) {
                return null;
            }
            segments.add(String.valueOf(definitionId));
            cursor = cursor.getParentStepParameter();
        }

        Collections.reverse(segments);
        return toAddress(parameter.getStep().getCode(), String.join("/", segments));
    }

    private String toAddress(String stepCode, String definitionPath) {
        if (stepCode == null || definitionPath == null) {
            return null;
        }
        String s = stepCode.trim();
        String p = definitionPath.trim();
        if (s.isEmpty() || p.isEmpty()) {
            return null;
        }
        return s + "|" + p;
    }

    private List<FormulaReference> safeReferences(ComputationFormula formula) {
        return formula.getReferences() == null ? List.of() : formula.getReferences();
    }
}