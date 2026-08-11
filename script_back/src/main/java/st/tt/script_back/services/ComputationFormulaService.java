package st.tt.script_back.services;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.ComputationFormulaDto;
import st.tt.script_back.dto.FormulaReferenceDto;
import st.tt.script_back.entities.ComputationFormula;
import st.tt.script_back.entities.FormulaReference;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.entities.Step;
import st.tt.script_back.entities.StepParameter;
import st.tt.script_back.enums.ParameterValueType;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.RoundingPolicy;
import st.tt.script_back.mappers.ComputationFormulaMapper;
import st.tt.script_back.repositories.ComputationFormulaRepository;
import st.tt.script_back.repositories.RecipeRepository;
import st.tt.script_back.repositories.StepParameterRepository;
import st.tt.script_back.repositories.StepRepository;

@Service
public class ComputationFormulaService {

    private static final Pattern SLOT_PATTERN = Pattern.compile("\\{(\\d+)\\}");

    private final ComputationFormulaRepository computationFormulaRepository;
    private final RecipeRepository recipeRepository;
    private final StepRepository stepRepository;
    private final StepParameterRepository stepParameterRepository;
    private final ComputationFormulaMapper computationFormulaMapper;
    private final ComputationEvaluationService computationEvaluationService;

    public ComputationFormulaService(
            ComputationFormulaRepository computationFormulaRepository,
            RecipeRepository recipeRepository,
            StepRepository stepRepository,
            StepParameterRepository stepParameterRepository,
            ComputationFormulaMapper computationFormulaMapper,
            ComputationEvaluationService computationEvaluationService) {
        this.computationFormulaRepository = computationFormulaRepository;
        this.recipeRepository = recipeRepository;
        this.stepRepository = stepRepository;
        this.stepParameterRepository = stepParameterRepository;
        this.computationFormulaMapper = computationFormulaMapper;
        this.computationEvaluationService = computationEvaluationService;
    }

    @Transactional(readOnly = true)
    public List<ComputationFormulaDto> getFormulasByRecipe(Long recipeId) {
        Recipe recipe = requireRecipe(recipeId);
        ensureGoldenRecipe(recipe);
        return computationFormulaMapper.toDtoList(computationFormulaRepository.findByRecipeIdWithReferences(recipeId));
    }

    @Transactional(readOnly = true)
    public ComputationFormulaDto getFormula(Long formulaId) {
        ComputationFormula formula = computationFormulaRepository.findByIdWithReferences(formulaId)
                .orElseThrow(() -> new EntityNotFoundException("ComputationFormula with id " + formulaId + " not found"));
        return computationFormulaMapper.toDto(formula);
    }

    @Transactional
    public ComputationFormulaDto createFormula(Long recipeId, ComputationFormulaDto request) {
        if (request == null) {
            throw new IllegalArgumentException("ComputationFormula payload is required");
        }

        Recipe recipe = requireRecipe(recipeId);
        ensureGoldenRecipe(recipe);
        ensureRecipeNotFrozen(recipe);
        ensureRecipeIdConsistency(recipeId, request.getRecipeId());

        ComputationFormula formula = computationFormulaMapper.toEntity(request);
        formula.setRecipe(recipe);
        if (formula.getRoundingMode() == null) {
            formula.setRoundingMode(RoundingPolicy.HALF_UP);
        }

        normalizeFormula(formula);
        validateFormula(recipe, formula, request.getReferences(), null);
        attachReferences(formula, request.getReferences());

        ComputationFormula saved = computationFormulaRepository.save(formula);
        recomputeImpactedRecipes(recipe.getId());
        return computationFormulaMapper.toDto(saved);
    }

    @Transactional
    public ComputationFormulaDto updateFormula(Long formulaId, ComputationFormulaDto request) {
        if (request == null) {
            throw new IllegalArgumentException("ComputationFormula payload is required");
        }

        ComputationFormula existing = computationFormulaRepository.findByIdWithReferences(formulaId)
                .orElseThrow(() -> new EntityNotFoundException("ComputationFormula with id " + formulaId + " not found"));

        Recipe recipe = existing.getRecipe();
        if (recipe == null || recipe.getId() == null) {
            throw new IllegalStateException("ComputationFormula has no recipe context");
        }

        ensureGoldenRecipe(recipe);
        ensureRecipeNotFrozen(recipe);
        ensureRecipeIdConsistency(recipe.getId(), request.getRecipeId());

        computationFormulaMapper.updateEntityFromDto(request, existing);
        if (existing.getRoundingMode() == null) {
            existing.setRoundingMode(RoundingPolicy.HALF_UP);
        }

        normalizeFormula(existing);
        validateFormula(recipe, existing, request.getReferences(), existing.getId());

        synchronizeReferences(existing, request.getReferences());

        ComputationFormula saved = computationFormulaRepository.save(existing);
        recomputeImpactedRecipes(recipe.getId());
        return computationFormulaMapper.toDto(saved);
    }

    @Transactional
    public void deleteFormula(Long formulaId) {
        ComputationFormula existing = computationFormulaRepository.findById(formulaId)
                .orElseThrow(() -> new EntityNotFoundException("ComputationFormula with id " + formulaId + " not found"));

        Recipe recipe = existing.getRecipe();
        if (recipe == null || recipe.getId() == null) {
            throw new IllegalStateException("ComputationFormula has no recipe context");
        }

        ensureGoldenRecipe(recipe);
        ensureRecipeNotFrozen(recipe);
        computationFormulaRepository.delete(existing);
        recomputeImpactedRecipes(recipe.getId());
    }

    private void validateFormula(
            Recipe recipe,
            ComputationFormula formula,
            List<FormulaReferenceDto> referenceDtos,
            Long excludeFormulaIdForCycleCheck) {

        if (formula.getTargetStepCode() == null || formula.getTargetStepCode().isBlank()) {
            throw new IllegalArgumentException("targetStepCode is required");
        }
        
        if (formula.getTargetDefinitionPath() == null || formula.getTargetDefinitionPath().isBlank()) {
            throw new IllegalArgumentException("targetDefinitionPath is required");
        }
        if (formula.getExpression() == null || formula.getExpression().isBlank()) {
            throw new IllegalArgumentException("expression is required");
        }
        if (referenceDtos == null || referenceDtos.isEmpty()) {
            throw new IllegalArgumentException("references are required");
        }

        StepParameter target = resolveParameterByAddress(recipe.getId(), formula.getTargetStepCode(), formula.getTargetDefinitionPath())
                .orElseThrow(() -> new IllegalArgumentException("Formula target does not resolve in recipe structure"));

        if (target.getDefinition() == null || target.getDefinition().getValueType() != ParameterValueType.NUMBER) {
            throw new IllegalArgumentException("Formula target must resolve to a NUMBER parameter");
        }
        if (target.isLockedByGolden()) {
            throw new IllegalArgumentException("A formula target cannot be lockedByGolden");
        }

        validateExpressionAgainstReferences(formula.getExpression(), referenceDtos);

        Set<Integer> seenSlots = new HashSet<>();
        for (FormulaReferenceDto referenceDto : referenceDtos) {
            if (referenceDto == null) {
                throw new IllegalArgumentException("references cannot contain null values");
            }
            if (referenceDto.getSlot() == null || referenceDto.getSlot() <= 0) {
                throw new IllegalArgumentException("reference slot must be >= 1");
            }
            if (!seenSlots.add(referenceDto.getSlot())) {
                throw new IllegalArgumentException("Duplicate reference slot: " + referenceDto.getSlot());
            }
            if (referenceDto.getStepCode() == null || referenceDto.getStepCode().isBlank()) {
                throw new IllegalArgumentException("reference stepCode is required");
            }
            if (referenceDto.getDefinitionPath() == null || referenceDto.getDefinitionPath().isBlank()) {
                throw new IllegalArgumentException("reference definitionPath is required");
            }

            StepParameter source = resolveParameterByAddress(recipe.getId(), referenceDto.getStepCode(), referenceDto.getDefinitionPath())
                    .orElseThrow(() -> new IllegalArgumentException("Formula reference does not resolve in recipe structure"));

            if (source.getDefinition() == null || source.getDefinition().getValueType() != ParameterValueType.NUMBER) {
                throw new IllegalArgumentException("Formula references must resolve to NUMBER parameters");
            }
        }

        detectCycles(recipe.getId(), formula, referenceDtos, excludeFormulaIdForCycleCheck);
    }

    private void validateExpressionAgainstReferences(String expression, List<FormulaReferenceDto> references) {
        Matcher matcher = SLOT_PATTERN.matcher(expression);
        Set<Integer> slotsInExpression = new HashSet<>();
        while (matcher.find()) {
            slotsInExpression.add(Integer.parseInt(matcher.group(1)));
        }

        if (slotsInExpression.isEmpty()) {
            throw new IllegalArgumentException("expression must reference at least one slot like {1}");
        }

        Set<Integer> slotsInReferences = new HashSet<>();
        for (FormulaReferenceDto reference : references) {
            if (reference != null && reference.getSlot() != null) {
                slotsInReferences.add(reference.getSlot());
            }
        }

        if (!slotsInExpression.equals(slotsInReferences)) {
            throw new IllegalArgumentException("expression slots and reference slots must match exactly");
        }
    }

    private void detectCycles(
            Long recipeId,
            ComputationFormula candidate,
            List<FormulaReferenceDto> candidateReferences,
            Long excludeFormulaId) {

        List<ComputationFormula> existingFormulas = computationFormulaRepository.findByRecipeIdWithReferences(recipeId);
        Map<String, Set<String>> graph = new HashMap<>();

        for (ComputationFormula formula : existingFormulas) {
            if (excludeFormulaId != null && formula.getId() != null && formula.getId().equals(excludeFormulaId)) {
                continue;
            }

            String target = toAddress(formula.getTargetStepCode(), formula.getTargetDefinitionPath());
            graph.computeIfAbsent(target, ignored -> new HashSet<>());

            for (FormulaReference reference : formula.getReferences()) {
                String source = toAddress(reference.getStepCode(), reference.getDefinitionPath());
                graph.computeIfAbsent(source, ignored -> new HashSet<>()).add(target);
            }
        }

        String candidateTarget = toAddress(candidate.getTargetStepCode(), candidate.getTargetDefinitionPath());
        graph.computeIfAbsent(candidateTarget, ignored -> new HashSet<>());

        for (FormulaReferenceDto reference : candidateReferences) {
            String source = toAddress(reference.getStepCode(), reference.getDefinitionPath());
            graph.computeIfAbsent(source, ignored -> new HashSet<>()).add(candidateTarget);
        }

        ensureAcyclicGraph(graph);
    }

    private void ensureAcyclicGraph(Map<String, Set<String>> graph) {
        Map<String, Integer> state = new HashMap<>();
        for (String node : graph.keySet()) {
            if (state.getOrDefault(node, 0) == 0) {
                dfsCheckCycle(node, graph, state);
            }
        }
    }

    private void dfsCheckCycle(String node, Map<String, Set<String>> graph, Map<String, Integer> state) {
        state.put(node, 1);
        for (String next : graph.getOrDefault(node, Set.of())) {
            int nextState = state.getOrDefault(next, 0);
            if (nextState == 1) {
                throw new IllegalArgumentException("Cycle detected in computation formulas");
            }
            if (nextState == 0) {
                dfsCheckCycle(next, graph, state);
            }
        }
        state.put(node, 2);
    }

    private void attachReferences(ComputationFormula formula, List<FormulaReferenceDto> referenceDtos) {
        List<FormulaReference> references = new ArrayList<>();
        for (FormulaReferenceDto referenceDto : referenceDtos) {
            FormulaReference reference = computationFormulaMapper.toReferenceEntity(referenceDto);
            reference.setFormula(formula);
            reference.setStepCode(reference.getStepCode() == null ? null : reference.getStepCode().trim());
            reference.setDefinitionPath(reference.getDefinitionPath() == null ? null : reference.getDefinitionPath().trim());
            references.add(reference);
        }
        formula.getReferences().addAll(references);
    }

    private void synchronizeReferences(ComputationFormula formula, List<FormulaReferenceDto> referenceDtos) {
        Map<Integer, FormulaReference> existingBySlot = new HashMap<>();
        for (FormulaReference existing : formula.getReferences()) {
            if (existing != null && existing.getSlot() != null) {
                existingBySlot.put(existing.getSlot(), existing);
            }
        }

        Set<Integer> requestedSlots = new HashSet<>();
        for (FormulaReferenceDto referenceDto : referenceDtos) {
            Integer slot = referenceDto.getSlot();
            requestedSlots.add(slot);

            FormulaReference reference = existingBySlot.get(slot);
            if (reference == null) {
                reference = new FormulaReference();
                reference.setFormula(formula);
                formula.getReferences().add(reference);
            }

            reference.setSlot(slot);
            reference.setStepCode(referenceDto.getStepCode() == null ? null : referenceDto.getStepCode().trim());
            reference.setDefinitionPath(referenceDto.getDefinitionPath() == null
                    ? null
                    : referenceDto.getDefinitionPath().trim());
        }

        formula.getReferences().removeIf(reference -> reference.getSlot() == null
                || !requestedSlots.contains(reference.getSlot()));
    }

    private Optional<StepParameter> resolveParameterByAddress(Long recipeId, String stepCode, String definitionPath) {
        if (recipeId == null || stepCode == null || stepCode.isBlank() || definitionPath == null || definitionPath.isBlank()) {
            return Optional.empty();
        }

        Step step = stepRepository.findByRecipeIdAndCode(recipeId, stepCode.trim()).orElse(null);
        if (step == null || step.getId() == null) {
            return Optional.empty();
        }

        List<StepParameter> parameters = stepParameterRepository.findByStepIdWithDefinitionAndSelectedOption(step.getId());
        List<Long> path = parsePath(definitionPath);
        if (path.isEmpty()) {
            return Optional.empty();
        }

        Long expectedParentId = null;
        StepParameter current = null;
        for (Long definitionId : path) {
            StepParameter next = null;
            for (StepParameter parameter : parameters) {
                Long parameterDefinitionId = parameter.getDefinition() != null ? parameter.getDefinition().getId() : null;
                Long parameterParentId = parameter.getParentStepParameter() != null
                        ? parameter.getParentStepParameter().getId()
                        : null;
                if (definitionId.equals(parameterDefinitionId) &&
                        ((expectedParentId == null && parameterParentId == null)
                                || (expectedParentId != null && expectedParentId.equals(parameterParentId)))) {
                    next = parameter;
                    break;
                }
            }

            if (next == null) {
                return Optional.empty();
            }

            current = next;
            expectedParentId = next.getId();
        }

        return Optional.ofNullable(current);
    }

    private List<Long> parsePath(String definitionPath) {
        String[] tokens = definitionPath.trim().split("/");
        List<Long> path = new ArrayList<>(tokens.length);
        for (String token : tokens) {
            String normalized = token.trim();
            if (normalized.isEmpty()) {
                throw new IllegalArgumentException("definitionPath contains empty segment");
            }
            try {
                path.add(Long.parseLong(normalized));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("definitionPath contains non numeric segment: " + normalized);
            }
        }
        return path;
    }

    private Recipe requireRecipe(Long recipeId) {
        return recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));
    }

    private void ensureGoldenRecipe(Recipe recipe) {
        if (recipe.getRecipeKind() != RecipeKind.GOLDEN) {
            throw new IllegalArgumentException("Computation formulas can only be managed on GOLDEN recipes");
        }
    }

    private void ensureRecipeNotFrozen(Recipe recipe) {
        if (recipe.isFrozen()) {
            throw new IllegalStateException("Frozen recipe cannot be modified");
        }
    }

    private void ensureRecipeIdConsistency(Long expectedRecipeId, Long requestRecipeId) {
        if (requestRecipeId != null && !requestRecipeId.equals(expectedRecipeId)) {
            throw new IllegalArgumentException("recipeId cannot be changed");
        }
    }

    private void normalizeFormula(ComputationFormula formula) {
        formula.setTargetStepCode(formula.getTargetStepCode() == null ? null : formula.getTargetStepCode().trim());
        formula.setTargetDefinitionPath(formula.getTargetDefinitionPath() == null ? null : formula.getTargetDefinitionPath().trim());
        formula.setExpression(formula.getExpression() == null ? null : formula.getExpression().trim());
    }

    private String toAddress(String stepCode, String definitionPath) {
        return stepCode.trim() + "|" + definitionPath.trim();
    }

    private void recomputeImpactedRecipes(Long goldenRecipeId) {
        for (Long recipeId : collectImpactedRecipeIds(goldenRecipeId)) {
            computationEvaluationService.recomputeRecipeComputedParameters(recipeId);
        }
    }

    private Set<Long> collectImpactedRecipeIds(Long goldenRecipeId) {
        Set<Long> impacted = new HashSet<>();
        ArrayDeque<Long> queue = new ArrayDeque<>();
        queue.add(goldenRecipeId);

        while (!queue.isEmpty()) {
            Long current = queue.removeFirst();
            if (!impacted.add(current)) {
                continue;
            }

            List<Recipe> children = recipeRepository.findByParentRecipeIdOrderByVersionDesc(current);
            for (Recipe child : children) {
                if (child.getId() != null) {
                    queue.addLast(child.getId());
                }
            }
        }

        return impacted;
    }
}