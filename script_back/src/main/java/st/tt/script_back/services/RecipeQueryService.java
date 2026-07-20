package st.tt.script_back.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import st.tt.script_back.dto.RecipeMatrixRowDto;
import st.tt.script_back.dto.StepDto;
import st.tt.script_back.dto.StepParameterDto;
import st.tt.script_back.dto.StepParameterGridRowDto;
import st.tt.script_back.entities.ParameterOption;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.entities.Step;
import st.tt.script_back.entities.StepParameter;
import st.tt.script_back.enums.ActivationState;
import st.tt.script_back.enums.ParameterValueType;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.StepKind;
import st.tt.script_back.mappers.ParameterOptionMapper;
import st.tt.script_back.mappers.RecipeMapper;
import st.tt.script_back.mappers.StepMapper;
import st.tt.script_back.mappers.StepParameterMapper;
import st.tt.script_back.repositories.ParameterOptionRepository;
import st.tt.script_back.repositories.RecipeRepository;
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
    private final StepParameterRepository stepParameterRepository;
    private final ParameterOptionRepository parameterOptionRepository;
    private final RecipeMapper recipeMapper;
    private final ParameterOptionMapper parameterOptionMapper;
    private final StepMapper stepMapper;
    private final StepParameterMapper stepParameterMapper;

    public RecipeQueryService(
            RecipeRepository recipeRepository,
            StepRepository stepRepository,
            StepParameterRepository stepParameterRepository,
            RecipeMapper recipeMapper,
            ParameterOptionRepository parameterOptionRepository,
            ParameterOptionMapper parameterOptionMapper,
            StepMapper stepMapper,
            StepParameterMapper stepParameterMapper) {
        this.recipeRepository = recipeRepository;
        this.stepRepository = stepRepository;
        this.stepParameterRepository = stepParameterRepository;
        this.recipeMapper = recipeMapper;
        this.parameterOptionRepository = parameterOptionRepository;
        this.parameterOptionMapper = parameterOptionMapper;
        this.stepMapper = stepMapper;
        this.stepParameterMapper = stepParameterMapper;
    }

    @Transactional(readOnly = true)
    public List<RecipeDto> getRecipes(RecipeKind recipeKind, Boolean golden) {
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
    public RecipeDto getRecipe(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));
        return recipeMapper.toDto(recipe);
    }

    @Transactional(readOnly = true)
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
    public List<StepParameterDto> getStepParameters(Long stepId) {
        ensureStepExists(stepId);

        List<StepParameter> parameters = stepParameterRepository.findByStepIdWithDefinitionAndSelectedOption(stepId);
        return stepParameterMapper.toDtoList(parameters);
    }

    @Transactional(readOnly = true)
    public StepParameterDto getStepParameter(Long stepParameterId) {
        StepParameter parameter = stepParameterRepository.findById(stepParameterId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StepParameter with id " + stepParameterId + " not found"));
        return stepParameterMapper.toDto(parameter);
    }

    @Transactional(readOnly = true)
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
            return new RecipeMatrixDto(recipeId, List.of(), List.of());
        }

        List<Long> stepIds = steps.stream().map(Step::getId).toList();
        List<StepParameter> parameters = stepParameterRepository.findByStepIdsWithDefinitionAndSelectedOption(stepIds);

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
                                            false);
                                }

                                String displayValue = cellParameter.getSelectedOption() != null
                                        ? cellParameter.getSelectedOption().getLabel()
                                        : (cellParameter.getValueJson() != null ? cellParameter.getValueJson() : "-");

                                boolean editable = !cellParameter.isLockedByGolden()
                                        && cellParameter.getActivationState() == ActivationState.ENABLED;

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
                                        false);
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

        return new RecipeMatrixDto(recipeId, columns, rows);
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
        if (!recipeRepository.existsById(recipeId)) {
            throw new EntityNotFoundException("Recipe with id " + recipeId + " not found");
        }
    }

    private void ensureStepExists(Long stepId) {
        if (!stepRepository.existsById(stepId)) {
            throw new EntityNotFoundException("Step with id " + stepId + " not found");
        }
    }
}