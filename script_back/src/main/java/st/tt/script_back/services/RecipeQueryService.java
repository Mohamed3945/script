package st.tt.script_back.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.ParameterOptionDto;
import st.tt.script_back.dto.RecipeDto;
import st.tt.script_back.dto.RecipeMatrixCellDto;
import st.tt.script_back.dto.RecipeMatrixColumnDto;
import st.tt.script_back.dto.RecipeMatrixDto;
import st.tt.script_back.dto.RecipeMatrixEndpointCellDto;
import st.tt.script_back.dto.RecipeMatrixRowDto;
import st.tt.script_back.dto.StepDto;
import st.tt.script_back.dto.StepParameterDto;
import st.tt.script_back.dto.StepParameterGridRowDto;
import st.tt.script_back.entities.ParameterOption;
import st.tt.script_back.entities.ComputationFormula;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.entities.Step;
import st.tt.script_back.entities.StepEndpoint;
import st.tt.script_back.entities.StepEndpointCondition;
import st.tt.script_back.entities.StepParameter;
import st.tt.script_back.enums.ActivationState;
import st.tt.script_back.enums.ParameterValueType;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.RoleCode;
import st.tt.script_back.enums.StepKind;
import st.tt.script_back.mappers.ParameterOptionMapper;
import st.tt.script_back.mappers.RecipeMapper;
import st.tt.script_back.mappers.StepMapper;
import st.tt.script_back.mappers.StepParameterMapper;
import st.tt.script_back.repositories.ComputationFormulaRepository;
import st.tt.script_back.repositories.ParameterOptionRepository;
import st.tt.script_back.repositories.RecipeRepository;
import st.tt.script_back.repositories.StepEndpointRepository;
import st.tt.script_back.repositories.StepParameterRepository;
import st.tt.script_back.repositories.StepRepository;

/**
 * Read-only query service for recipes, steps, parameters, and matrix projections.
 * <p>
 * This service centralizes UI-facing aggregation logic and enforces consistent projections for list, grid,
 * and matrix endpoints.
 */
@Service
public class RecipeQueryService {

    private final RecipeRepository recipeRepository;
    private final StepRepository stepRepository;
    private final StepEndpointRepository stepEndpointRepository;
    private final StepParameterRepository stepParameterRepository;
    private final ComputationFormulaRepository computationFormulaRepository;
    private final ParameterOptionRepository parameterOptionRepository;
    private final RecipeMapper recipeMapper;
    private final ParameterOptionMapper parameterOptionMapper;
    private final StepMapper stepMapper;
    private final StepParameterMapper stepParameterMapper;
    private final AuthService authService;

    public RecipeQueryService(
            RecipeRepository recipeRepository,
            StepRepository stepRepository,
            StepEndpointRepository stepEndpointRepository,
            StepParameterRepository stepParameterRepository,
            ComputationFormulaRepository computationFormulaRepository,
            RecipeMapper recipeMapper,
            ParameterOptionRepository parameterOptionRepository,
            ParameterOptionMapper parameterOptionMapper,
            StepMapper stepMapper,
            StepParameterMapper stepParameterMapper,
            AuthService authService) {
        this.recipeRepository = recipeRepository;
        this.stepRepository = stepRepository;
        this.stepEndpointRepository = stepEndpointRepository;
        this.stepParameterRepository = stepParameterRepository;
        this.computationFormulaRepository = computationFormulaRepository;
        this.recipeMapper = recipeMapper;
        this.parameterOptionRepository = parameterOptionRepository;
        this.parameterOptionMapper = parameterOptionMapper;
        this.stepMapper = stepMapper;
        this.stepParameterMapper = stepParameterMapper;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    /**
     * Recherche les recettes selon le rôle courant et les filtres demandés.
     *
     * <p>Les utilisateurs qui ne sont pas super-utilisateurs sont limités à
     * leurs recettes dérivées. Les super-utilisateurs peuvent consulter toute
     * la collection et appliquer les filtres {@code recipeKind} ou {@code golden}.
     *
     * @param recipeKind type de recette à rechercher, ou {@code null}
     * @param golden indique si la recherche doit sélectionner les recettes golden
     * @return recettes mappées en DTO et triées par date de révision décroissante
     */
    public List<RecipeDto> getRecipes(RecipeKind recipeKind, Boolean golden) {
        if (!authService.isSuperUser()) {
            return recipeMapper.toDtoList(recipeRepository.findByRecipeKindAndCreatorIdOrderByReviseTimeDesc(
                    RecipeKind.DERIVED,
                    authService.getCurrentUserId()));
        }

        List<Recipe> recipes;

        if (recipeKind != null) {
            recipes = recipeRepository.findByRecipeKindOrderByReviseTimeDesc(recipeKind);
        } else if (Boolean.TRUE.equals(golden)) {
            recipes = recipeRepository.findByRecipeKindOrderByReviseTimeDesc(RecipeKind.GOLDEN);
        } else if (Boolean.FALSE.equals(golden)) {
            recipes = recipeRepository.findByRecipeKindNotOrderByReviseTimeDesc(RecipeKind.GOLDEN);
        } else {
            recipes = recipeRepository.findAll(Sort.by(Sort.Direction.DESC, "reviseTime"));
        }

        return recipeMapper.toDtoList(recipes);
    }

    @Transactional(readOnly = true)
    /**
     * Charge une recette après vérification de son existence et de son accessibilité.
     *
     * @param recipeId identifiant de la recette
     * @return représentation DTO de la recette
     * @throws EntityNotFoundException si aucune recette ne correspond à l'identifiant
     */
    public RecipeDto getRecipe(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));
        ensureRecipeAccessible(recipe);
        return recipeMapper.toDto(recipe);
    }

    @Transactional(readOnly = true)
    /**
     * Charge les étapes d'une recette dans l'ordre d'exécution.
     *
     * @param recipeId identifiant de la recette
     * @param stepKind filtre optionnel sur le type d'étape
     * @return étapes converties en DTO
     * @throws EntityNotFoundException si la recette n'existe pas
     */
    public List<StepDto> getRecipeSteps(Long recipeId, StepKind stepKind) {
        ensureRecipeExists(recipeId);

        List<Step> steps = (stepKind == null)
                ? stepRepository.findByRecipeIdOrderByOrderIndexAsc(recipeId)
                : stepRepository.findByRecipeIdAndStepKindOrderByOrderIndexAsc(recipeId, stepKind);

        return stepMapper.toDtoList(steps);
    }

    @Transactional(readOnly = true)
    public StepDto getStep(Long stepId) {
        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException("Step with id " + stepId + " not found"));
        return stepMapper.toDto(step);
    }

    @Transactional(readOnly = true)
    /**
     * Retourne les paramètres d'une étape et enrichit chaque DTO avec son état calculé.
     *
     * @param stepId identifiant de l'étape
     * @return paramètres avec définition, option sélectionnée et indicateur calculé
     * @throws EntityNotFoundException si l'étape n'existe pas
     */
    public List<StepParameterDto> getStepParameters(Long stepId) {
        ensureStepExists(stepId);

        List<StepParameter> parameters = stepParameterRepository.findByStepIdWithDefinitionAndSelectedOption(stepId);
        Set<String> computedTargets = resolveComputedTargetsForStepParameters(parameters);
        return parameters.stream()
                .map(parameter -> {
                    StepParameterDto dto = stepParameterMapper.toDto(parameter);
                    dto.setComputed(isComputedParameter(parameter, computedTargets));
                    return dto;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public StepParameterDto getStepParameter(Long stepParameterId) {
        StepParameter parameter = stepParameterRepository.findById(stepParameterId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StepParameter with id " + stepParameterId + " not found"));
        Set<String> computedTargets = resolveComputedTargetsForRecipeId(
            parameter.getStep() != null && parameter.getStep().getRecipe() != null
                ? parameter.getStep().getRecipe().getId()
                : null);
        StepParameterDto dto = stepParameterMapper.toDto(parameter);
        dto.setComputed(isComputedParameter(parameter, computedTargets));
        return dto;
    }

    @Transactional(readOnly = true)
    /**
     * Détermine les paramètres calculés impactés par un paramètre source.
     *
     * <p>La recherche parcourt le graphe de dépendances des formules de la recette
     * golden associée, puis conserve uniquement les paramètres calculés présents
     * dans la recette consultée.
     *
     * @param stepParameterId identifiant du paramètre source
     * @return identifiants des paramètres calculés impactés
     * @throws EntityNotFoundException si le paramètre source n'existe pas
     */
    public List<Long> getComputedDependentsByStepParameterId(Long stepParameterId) {
        StepParameter source = stepParameterRepository.findById(stepParameterId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StepParameter with id " + stepParameterId + " not found"));

        Long recipeId = source.getStep() != null && source.getStep().getRecipe() != null
                ? source.getStep().getRecipe().getId()
                : null;
        if (recipeId == null) {
            return List.of();
        }

        Long goldenRecipeId = resolveGoldenRecipeId(recipeId);
        List<ComputationFormula> formulas = computationFormulaRepository.findByRecipeIdWithReferences(goldenRecipeId);
        if (formulas.isEmpty()) {
            return List.of();
        }

        Map<String, Set<String>> dependencyGraph = buildDependencyGraph(formulas);

        String sourceStepCode = source.getStep() != null ? source.getStep().getCode() : null;
        String sourceDefinitionPath = buildDefinitionPath(source);
        if (sourceStepCode == null || sourceStepCode.isBlank()
                || sourceDefinitionPath == null || sourceDefinitionPath.isBlank()) {
            return List.of();
        }

        String sourceAddress = toStructuralAddress(sourceStepCode.trim(), sourceDefinitionPath);

        Set<String> impactedAddresses = collectImpactedAddresses(Set.of(sourceAddress), dependencyGraph);

        if (impactedAddresses.isEmpty()) {
            return List.of();
        }

        Set<String> computedTargets = resolveComputedTargetsForRecipeId(recipeId);
        List<StepParameter> recipeParameters = stepParameterRepository.findByRecipeIdWithStepAndDefinition(recipeId);

        return recipeParameters.stream()
                .filter(parameter -> {
                    String stepCode = parameter.getStep() != null ? parameter.getStep().getCode() : null;
                    String definitionPath = buildDefinitionPath(parameter);

                    if (stepCode == null || stepCode.isBlank()
                            || definitionPath == null || definitionPath.isBlank()) {
                        return false;
                    }

                    String address = toStructuralAddress(stepCode.trim(), definitionPath);
                    return impactedAddresses.contains(address) && computedTargets.contains(address);
                })
                .map(StepParameter::getId)
                .filter(id -> id != null)
                .toList();
    }

    @Transactional(readOnly = true)
    /**
     * Construit la projection tabulaire des paramètres d'une recette.
     *
     * <p>Les lignes sont ordonnées par groupe, position dans le groupe puis nom
     * de définition. Une recette sans étape produit une liste vide.
     *
     * @param recipeId identifiant de la recette
     * @return lignes de grille destinées à l'éditeur de recette
     */
    public List<StepParameterGridRowDto> getRecipeStepParameterGrid(Long recipeId) {
        ensureRecipeExists(recipeId);

        List<Step> steps = stepRepository.findByRecipeIdOrderByOrderIndexAsc(recipeId);
        if (steps.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Step> stepById = new HashMap<>();
        List<Long> stepIds = steps.stream().map(Step::getId).toList();
        for (Step step : steps) {
            stepById.put(step.getId(), step);
        }

        List<StepParameter> parameters = stepParameterRepository.findByStepIdsWithDefinitionAndSelectedOption(stepIds);

        return parameters.stream()
                .sorted(Comparator
                        .comparing((StepParameter p) -> {
                            if (p.getDefinition() == null || p.getDefinition().getParameterGroupRef() == null
                                    || p.getDefinition().getParameterGroupRef().getOrderIndex() == null) {
                                return Integer.MAX_VALUE;
                            }
                            return p.getDefinition().getParameterGroupRef().getOrderIndex();
                        })
                        .thenComparing(p -> p.getDefinition() != null && p.getDefinition().getOrderIndexInGroup() != null
                                ? p.getDefinition().getOrderIndexInGroup()
                                : Integer.MAX_VALUE)
                        .thenComparing(p -> p.getDefinition() != null && p.getDefinition().getName() != null
                                ? p.getDefinition().getName()
                                : ""))
                .map(parameter -> toGridRow(recipeId, stepById, parameter))
                .toList();
    }

    /**
     * Builds the matrix view used by the frontend recipe editor.
     */
    @Transactional(readOnly = true)
    public RecipeMatrixDto getRecipeMatrix(Long recipeId) {
        ensureRecipeExists(recipeId);

        List<Step> steps = stepRepository.findByRecipeIdOrderByOrderIndexAsc(recipeId);
        if (steps.isEmpty()) {
            return new RecipeMatrixDto(recipeId, List.of(), List.of(), List.of());
        }

        List<Long> stepIds = steps.stream().map(Step::getId).toList();
        List<StepParameter> parameters = stepParameterRepository.findByStepIdsWithDefinitionAndSelectedOption(stepIds);
        Set<String> computedTargets = resolveComputedTargetsForRecipeId(recipeId);
        Set<String> computedFromModifiedTargets = resolveComputedTargetsFromModifiedSources(
            recipeId,
            parameters,
            computedTargets);
        List<StepEndpoint> endpoints = stepEndpointRepository.findByStepIdsWithConditions(stepIds);

        Map<Long, StepEndpoint> endpointByStepId = new HashMap<>();
        for (StepEndpoint endpoint : endpoints) {
            if (endpoint.getStep() == null || endpoint.getStep().getId() == null) {
                continue;
            }
            endpointByStepId.putIfAbsent(endpoint.getStep().getId(), endpoint);
        }

        Map<Long, List<ParameterOptionDto>> optionsByDefinitionId = new HashMap<>();
        Set<Long> enumDefinitionIds = parameters.stream()
                .filter(p -> p.getDefinition() != null
                        && p.getDefinition().getValueType() == ParameterValueType.ENUM)
                .map(p -> p.getDefinition().getId())
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        for (Long definitionId : enumDefinitionIds) {
            List<ParameterOption> options = parameterOptionRepository.findByDefinitionIdOrderByOrderIndexAsc(
                    definitionId);
            optionsByDefinitionId.put(definitionId, parameterOptionMapper.toDtoList(options));
        }

        List<RecipeMatrixColumnDto> columns = steps.stream()
                .map(step -> new RecipeMatrixColumnDto(
                        step.getId(),
                        step.getCode(),
                        step.getName(),
                        step.getStepKind(),
                        step.getOrderIndex()))
                .toList();

        Map<Long, Map<Long, StepParameter>> parameterByDefinitionAndStep = new HashMap<>();
        Map<Long, StepParameter> representativeParameterByDefinition = new LinkedHashMap<>();

        for (StepParameter parameter : parameters) {
            if (parameter.getDefinition() == null
                    || parameter.getDefinition().getId() == null
                    || parameter.getStep() == null
                    || parameter.getStep().getId() == null) {
                continue;
            }

            Long definitionId = parameter.getDefinition().getId();
            Long stepId = parameter.getStep().getId();

            parameterByDefinitionAndStep
                    .computeIfAbsent(definitionId, ignored -> new HashMap<>())
                    .put(stepId, parameter);

            representativeParameterByDefinition.putIfAbsent(definitionId, parameter);
        }

        List<StepParameter> sortedRepresentatives = representativeParameterByDefinition.values().stream()
                .sorted(Comparator
                        .comparing((StepParameter p) -> {
                            if (p.getDefinition() == null || p.getDefinition().getParameterGroupRef() == null
                                    || p.getDefinition().getParameterGroupRef().getOrderIndex() == null) {
                                return Integer.MAX_VALUE;
                            }
                            return p.getDefinition().getParameterGroupRef().getOrderIndex();
                        })
                        .thenComparing(p -> p.getDefinition() != null && p.getDefinition().getOrderIndexInGroup() != null
                                ? p.getDefinition().getOrderIndexInGroup()
                                : Integer.MAX_VALUE)
                        .thenComparing(p -> p.getDefinition() != null && p.getDefinition().getName() != null
                                ? p.getDefinition().getName()
                                : ""))
                .toList();

        List<RecipeMatrixRowDto> rows = sortedRepresentatives.stream()
                .map(parameter -> {
                    Long definitionId = parameter.getDefinition().getId();

                    List<RecipeMatrixCellDto> cells = steps.stream()
                            .map(step -> {
                                StepParameter cellParameter = parameterByDefinitionAndStep
                                        .getOrDefault(definitionId, Map.of())
                                        .get(step.getId());

                                if (cellParameter == null) {
                                    return new RecipeMatrixCellDto(
                                            step.getId(),
                                            null,
                                            definitionId,
                                            parameter.getDefinition().getValueType(),
                                            "-",
                                            null,
                                            null,
                                            null,
                                            optionsByDefinitionId.getOrDefault(definitionId, List.of()),
                                            null,
                                            false,
                                            false,
                                            false,
                                            null,
                                            null,
                                            false,
                                            false);
                                }

                                String displayValue = cellParameter.getSelectedOption() != null
                                        ? cellParameter.getSelectedOption().getLabel()
                                        : (cellParameter.getValueJson() != null ? cellParameter.getValueJson() : "-");

                                boolean editable = !cellParameter.isLockedByGolden()
                                        && cellParameter.getActivationState() == ActivationState.ENABLED;

                                boolean computed = isComputedParameter(cellParameter, computedTargets);
                                boolean computedFromModified = computed
                                    && isComputedParameter(cellParameter, computedFromModifiedTargets);

                                return new RecipeMatrixCellDto(
                                        step.getId(),
                                        cellParameter.getId(),
                                        definitionId,
                                        cellParameter.getDefinition() != null ? cellParameter.getDefinition().getValueType() : null,
                                        displayValue,
                                        cellParameter.getValueJson(),
                                        cellParameter.getSelectedOption() != null ? cellParameter.getSelectedOption().getId() : null,
                                        cellParameter.getSelectedOption() != null ? cellParameter.getSelectedOption().getLabel() : null,
                                        optionsByDefinitionId.getOrDefault(definitionId, List.of()),
                                        cellParameter.getActivationState(),
                                        cellParameter.isLockedByGolden(),
                                        editable,
                                        cellParameter.isUserModified(),
                                        cellParameter.getComputationStatus(),
                                        cellParameter.getComputedAt(),
                                        computedFromModified,
                                        computed);
                            })
                            .toList();

                    return new RecipeMatrixRowDto(
                            definitionId,
                            parameter.getDefinition().getName(),
                            parameter.getDefinition().getAlias(),
                            parameter.getDefinition().getParameterGroupRef() != null
                                    ? parameter.getDefinition().getParameterGroupRef().getName()
                                    : null,
                            parameter.getDefinition().getParameterGroupRef() != null
                                    && parameter.getDefinition().getParameterGroupRef().getOrderIndex() != null
                                            ? parameter.getDefinition().getParameterGroupRef().getOrderIndex()
                                            : Integer.MAX_VALUE,
                            parameter.getDefinition().getValueType(),
                            cells);
                })
                .toList();

        List<RecipeMatrixEndpointCellDto> endpointRow = steps.stream()
                .map(step -> toEndpointCell(step.getId(), endpointByStepId.get(step.getId())))
                .toList();

        return new RecipeMatrixDto(recipeId, columns, rows, endpointRow);
    }

    private RecipeMatrixEndpointCellDto toEndpointCell(Long stepId, StepEndpoint endpoint) {
        if (endpoint == null || endpoint.getConditions() == null || endpoint.getConditions().isEmpty()) {
            return new RecipeMatrixEndpointCellDto(stepId, null, null, 0, "Time", false);
        }

        List<StepEndpointCondition> sortedConditions = endpoint.getConditions().stream()
                .sorted(Comparator.comparing(condition -> condition.getOrderIndex() == null
                        ? Integer.MAX_VALUE
                        : condition.getOrderIndex()))
                .toList();

        int conditionCount = sortedConditions.size();
        String summaryLabel = conditionCount == 1
                ? summarizeSingleCondition(sortedConditions.get(0))
                : conditionCount + " conditions";

        return new RecipeMatrixEndpointCellDto(
                stepId,
                endpoint.getId(),
                endpoint.getClause(),
                conditionCount,
                summaryLabel,
                endpoint.isLockedByGolden());
    }

    private String summarizeSingleCondition(StepEndpointCondition condition) {
        if (condition == null) {
            return "1 condition";
        }

        String parameterLabel = condition.getEndpointParameter() != null
                ? (condition.getEndpointParameter().getAlias() != null
                        ? condition.getEndpointParameter().getAlias()
                        : condition.getEndpointParameter().getName())
                : "parameter";

        String operator = condition.getOperator() != null ? condition.getOperator().name() : "EQ";

        String value = "-";
        if (condition.getSelectedOption() != null && condition.getSelectedOption().getLabel() != null) {
            value = condition.getSelectedOption().getLabel();
        } else if (condition.getValueJson() != null && !condition.getValueJson().isBlank()) {
            value = normalizeScalarForSummary(condition.getValueJson());
        }

        return parameterLabel + " " + operator + " " + value;
    }

    private String normalizeScalarForSummary(String rawValue) {
        String trimmed = rawValue.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    /**
     * Maps a single step-parameter entity to the flattened grid row projection.
     */
    private StepParameterGridRowDto toGridRow(Long recipeId, Map<Long, Step> stepById, StepParameter parameter) {
        Long stepId = parameter.getStep() != null ? parameter.getStep().getId() : null;
        Step step = stepById.get(stepId);

        return new StepParameterGridRowDto(
                recipeId,
                stepId,
                step != null ? step.getName() : null,
                step != null ? step.getStepKind() : null,
                step != null ? step.getCode() : null,
                step != null ? step.getOrderIndex() : null,

                parameter.getId(),
                parameter.getParentStepParameter() != null ? parameter.getParentStepParameter().getId() : null,
                parameter.getParentOrderScope(),
                parameter.getOrderIndex(),
                parameter.getDefinition() != null ? parameter.getDefinition().getId() : null,
                parameter.getDefinition() != null ? parameter.getDefinition().getName() : null,
                parameter.getDefinition() != null && parameter.getDefinition().getParameterGroupRef() != null
                    ? parameter.getDefinition().getParameterGroupRef().getName()
                    : null,
                parameter.getDefinition() != null && parameter.getDefinition().getParameterGroupRef() != null
                    ? parameter.getDefinition().getParameterGroupRef().getOrderIndex()
                    : null,
                buildDefinitionPath(parameter),
                parameter.getDefinition() != null ? parameter.getDefinition().getValueType() : null,
                parameter.getLabelOverride(),
                parameter.getValueJson(),
                parameter.getSelectedOption() != null ? parameter.getSelectedOption().getId() : null,
                parameter.getSelectedOption() != null ? parameter.getSelectedOption().getLabel() : null,
                parameter.getActivationState(),
                parameter.isLockedByGolden()
        );
    }

    private void ensureRecipeExists(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));
        ensureRecipeAccessible(recipe);
    }

    private void ensureRecipeAccessible(Recipe recipe) {
        if (authService.hasRole(RoleCode.SUPER)) {
            return;
        }
        Long currentUserId = authService.getCurrentUserId();
        if (recipe.getRecipeKind() == RecipeKind.DERIVED && currentUserId.equals(recipe.getCreatorId())) {
            return;
        }
        throw new EntityNotFoundException("Recipe with id " + recipe.getId() + " not found");
    }

    private void ensureStepExists(Long stepId) {
        if (!stepRepository.existsById(stepId)) {
            throw new EntityNotFoundException("Step with id " + stepId + " not found");
        }
    }

    private Set<String> resolveComputedTargetsForStepParameters(List<StepParameter> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return Set.of();
        }

        StepParameter first = parameters.get(0);
        Long recipeId = first.getStep() != null && first.getStep().getRecipe() != null
                ? first.getStep().getRecipe().getId()
                : null;
        return resolveComputedTargetsForRecipeId(recipeId);
    }

    private Set<String> resolveComputedTargetsForRecipeId(Long recipeId) {
        if (recipeId == null) {
            return Set.of();
        }

        Long goldenRecipeId = resolveGoldenRecipeId(recipeId);
        List<ComputationFormula> formulas = computationFormulaRepository.findByRecipeIdWithReferences(goldenRecipeId);

        if (formulas.isEmpty()) {
            return Set.of();
        }

        Set<String> targets = new HashSet<>();
        for (ComputationFormula formula : formulas) {
            String stepCode = formula.getTargetStepCode() == null ? null : formula.getTargetStepCode().trim();
            String definitionPath = formula.getTargetDefinitionPath() == null
                    ? null
                    : formula.getTargetDefinitionPath().trim();

            if (stepCode == null || stepCode.isEmpty() || definitionPath == null || definitionPath.isEmpty()) {
                continue;
            }

            targets.add(toStructuralAddress(stepCode, definitionPath));
        }

        return targets;
    }

    private Long resolveGoldenRecipeId(Long recipeId) {
        Recipe current = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));

        Set<Long> visited = new HashSet<>();
        while (current != null && current.getRecipeKind() != RecipeKind.GOLDEN) {
            if (current.getId() != null && !visited.add(current.getId())) {
                throw new IllegalStateException("Detected cycle while resolving golden ancestor for recipe " + recipeId);
            }

            Long parentId = current.getParentRecipe() != null ? current.getParentRecipe().getId() : null;
            if (parentId == null) {
                break;
            }

            current = recipeRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent recipe with id " + parentId + " not found"));
        }

        return current != null && current.getId() != null ? current.getId() : recipeId;
    }

    private Set<String> resolveComputedTargetsFromModifiedSources(
            Long recipeId,
            List<StepParameter> parameters,
            Set<String> computedTargets) {
        if (recipeId == null || parameters == null || parameters.isEmpty()
                || computedTargets == null || computedTargets.isEmpty()) {
            return Set.of();
        }

        Set<String> sourceAddresses = new HashSet<>();
        for (StepParameter parameter : parameters) {
            if (parameter == null || !parameter.isUserModified() || isComputedParameter(parameter, computedTargets)) {
                continue;
            }

            String stepCode = parameter.getStep() != null ? parameter.getStep().getCode() : null;
            String definitionPath = buildDefinitionPath(parameter);
            if (stepCode == null || stepCode.isBlank() || definitionPath == null || definitionPath.isBlank()) {
                continue;
            }

            sourceAddresses.add(toStructuralAddress(stepCode.trim(), definitionPath));
        }

        if (sourceAddresses.isEmpty()) {
            return Set.of();
        }

        Long goldenRecipeId = resolveGoldenRecipeId(recipeId);
        List<ComputationFormula> formulas = computationFormulaRepository.findByRecipeIdWithReferences(goldenRecipeId);
        if (formulas.isEmpty()) {
            return Set.of();
        }

        Map<String, Set<String>> dependencyGraph = buildDependencyGraph(formulas);
        if (dependencyGraph.isEmpty()) {
            return Set.of();
        }

        Set<String> impactedAddresses = collectImpactedAddresses(sourceAddresses, dependencyGraph);
        impactedAddresses.retainAll(computedTargets);
        return impactedAddresses;
    }

    private Map<String, Set<String>> buildDependencyGraph(List<ComputationFormula> formulas) {
        if (formulas == null || formulas.isEmpty()) {
            return Map.of();
        }

        Map<String, Set<String>> dependencyGraph = new HashMap<>();
        for (ComputationFormula formula : formulas) {
            String targetStepCode = formula.getTargetStepCode() == null ? null : formula.getTargetStepCode().trim();
            String targetDefinitionPath = formula.getTargetDefinitionPath() == null
                    ? null
                    : formula.getTargetDefinitionPath().trim();
            if (targetStepCode == null || targetStepCode.isBlank()
                    || targetDefinitionPath == null || targetDefinitionPath.isBlank()) {
                continue;
            }

            String targetAddress = toStructuralAddress(targetStepCode, targetDefinitionPath);
            dependencyGraph.computeIfAbsent(targetAddress, ignored -> new HashSet<>());

            if (formula.getReferences() == null) {
                continue;
            }

            for (var reference : formula.getReferences()) {
                String sourceStepCode = reference.getStepCode() == null ? null : reference.getStepCode().trim();
                String sourceDefinitionPath = reference.getDefinitionPath() == null
                        ? null
                        : reference.getDefinitionPath().trim();

                if (sourceStepCode == null || sourceStepCode.isBlank()
                        || sourceDefinitionPath == null || sourceDefinitionPath.isBlank()) {
                    continue;
                }

                String sourceAddress = toStructuralAddress(sourceStepCode, sourceDefinitionPath);
                dependencyGraph.computeIfAbsent(sourceAddress, ignored -> new HashSet<>()).add(targetAddress);
            }
        }

        return dependencyGraph;
    }

    private Set<String> collectImpactedAddresses(
            Set<String> sourceAddresses,
            Map<String, Set<String>> dependencyGraph) {
        if (sourceAddresses == null || sourceAddresses.isEmpty() || dependencyGraph == null || dependencyGraph.isEmpty()) {
            return Set.of();
        }

        Set<String> impactedAddresses = new HashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        for (String sourceAddress : sourceAddresses) {
            if (sourceAddress != null && !sourceAddress.isBlank()) {
                queue.add(sourceAddress);
            }
        }

        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            Set<String> targets = dependencyGraph.getOrDefault(current, Set.of());
            for (String target : targets) {
                if (impactedAddresses.add(target)) {
                    queue.addLast(target);
                }
            }
        }

        return impactedAddresses;
    }

    private boolean isComputedParameter(StepParameter parameter, Set<String> computedTargets) {
        if (parameter == null || computedTargets == null || computedTargets.isEmpty()) {
            return false;
        }

        String stepCode = parameter.getStep() != null ? parameter.getStep().getCode() : null;
        String definitionPath = buildDefinitionPath(parameter);

        if (stepCode == null || stepCode.isBlank() || definitionPath == null || definitionPath.isBlank()) {
            return false;
        }

        return computedTargets.contains(toStructuralAddress(stepCode.trim(), definitionPath));
    }

    private String buildDefinitionPath(StepParameter parameter) {
        List<String> tokens = new ArrayList<>();
        StepParameter cursor = parameter;
        int guard = 0;

        while (cursor != null) {
            if (guard++ > 256) {
                throw new IllegalStateException("Invalid parent chain depth for step parameter " + parameter.getId());
            }

            Long definitionId = cursor.getDefinition() != null ? cursor.getDefinition().getId() : null;
            if (definitionId == null) {
                return null;
            }

            tokens.add(String.valueOf(definitionId));
            cursor = cursor.getParentStepParameter();
        }

        Collections.reverse(tokens);
        return String.join("/", tokens);
    }

    private String toStructuralAddress(String stepCode, String definitionPath) {
        String normalizedStepCode = stepCode == null ? "" : stepCode.trim().toLowerCase();
        String normalizedDefinitionPath = definitionPath == null ? "" : definitionPath.trim().toLowerCase();
        return normalizedStepCode + "|" + normalizedDefinitionPath;
    }
}