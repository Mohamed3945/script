package st.tt.script_back.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.RecipeDto;
import st.tt.script_back.dto.StepDto;
import st.tt.script_back.dto.StepParameterDto;
import st.tt.script_back.dto.StepParameterGridRowDto;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.entities.Step;
import st.tt.script_back.entities.StepParameter;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.StepKind;
import st.tt.script_back.mappers.RecipeMapper;
import st.tt.script_back.mappers.StepMapper;
import st.tt.script_back.mappers.StepParameterMapper;
import st.tt.script_back.repositories.RecipeRepository;
import st.tt.script_back.repositories.StepParameterRepository;
import st.tt.script_back.repositories.StepRepository;

@Service
public class RecipeQueryService {

    private final RecipeRepository recipeRepository;
    private final StepRepository stepRepository;
    private final StepParameterRepository stepParameterRepository;
    private final RecipeMapper recipeMapper;
    private final StepMapper stepMapper;
    private final StepParameterMapper stepParameterMapper;

    public RecipeQueryService(
            RecipeRepository recipeRepository,
            StepRepository stepRepository,
            StepParameterRepository stepParameterRepository,
            RecipeMapper recipeMapper,
            StepMapper stepMapper,
            StepParameterMapper stepParameterMapper) {
        this.recipeRepository = recipeRepository;
        this.stepRepository = stepRepository;
        this.stepParameterRepository = stepParameterRepository;
        this.recipeMapper = recipeMapper;
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

        return parameters.stream().map(parameter -> toGridRow(recipeId, stepById, parameter)).toList();
    }

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
