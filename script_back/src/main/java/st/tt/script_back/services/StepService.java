package st.tt.script_back.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.StepDto;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.entities.Step;
import st.tt.script_back.enums.StepKind;
import st.tt.script_back.mappers.StepMapper;
import st.tt.script_back.repositories.RecipeRepository;
import st.tt.script_back.repositories.StepRepository;

@Service
public class StepService {

    private final StepRepository stepRepository;
    private final RecipeRepository recipeRepository;
    private final StepMapper stepMapper;
    private final ParameterActivationService parameterActivationService;

    public StepService(
            StepRepository stepRepository,
            RecipeRepository recipeRepository,
            StepMapper stepMapper,
            ParameterActivationService parameterActivationService) {
        this.stepRepository = stepRepository;
        this.recipeRepository = recipeRepository;
        this.stepMapper = stepMapper;
        this.parameterActivationService = parameterActivationService;
    }

    @Transactional
    public StepDto createStep(Long recipeId, StepDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Step payload is required");
        }

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));

        Step step = stepMapper.toEntity(request);
        step.setRecipe(recipe);

        if (step.getStepKind() == null) {
            step.setStepKind(StepKind.STEP);
        }

        if (step.getOrderIndex() == null) {
            if (step.getStepKind() == StepKind.PRESTEP) {
                step.setOrderIndex(0);
            } else {
                Step lastStep = stepRepository.findTopByRecipeIdOrderByOrderIndexDesc(recipeId);
                int next = lastStep == null || lastStep.getOrderIndex() == null ? 1 : lastStep.getOrderIndex() + 1;
                step.setOrderIndex(next);
            }
        }

        step.setCode(ensureUniqueCodeForRecipe(recipeId, step.getOrderIndex(), null));

        Step saved = stepRepository.save(step);
        return stepMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public StepDto getStep(Long stepId) {
        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException("Step with id " + stepId + " not found"));
        return stepMapper.toDto(step);
    }

    @Transactional
    public StepDto updateStep(Long stepId, StepDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Step payload is required");
        }

        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException("Step with id " + stepId + " not found"));

        if (request.getRecipeId() != null && step.getRecipe() != null
                && !request.getRecipeId().equals(step.getRecipe().getId())) {
            throw new IllegalArgumentException("recipeId cannot be changed");
        }

        stepMapper.updateEntityFromDto(request, step);
        if (step.getOrderIndex() == null) {
            throw new IllegalArgumentException("orderIndex is required");
        }

        Long recipeId = step.getRecipe() != null ? step.getRecipe().getId() : null;
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId is required");
        }

        step.setCode(ensureUniqueCodeForRecipe(recipeId, step.getOrderIndex(), step.getId()));

        Step saved = stepRepository.save(step);
        return stepMapper.toDto(saved);
    }

    @Transactional
    public void deleteStep(Long stepId) {
        Step existing = stepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException("Step with id " + stepId + " not found"));
        Long recipeId = existing.getRecipe() != null ? existing.getRecipe().getId() : null;
        stepRepository.delete(existing);
        if (recipeId != null) {
            parameterActivationService.recalculateRecipeActivationStates(recipeId);
        }
    }

    private String ensureUniqueCodeForRecipe(Long recipeId, Integer orderIndex, Long currentStepId) {
        String baseCode = "STEP_" + orderIndex;
        String candidate = baseCode;
        int suffix = 2;

        while (true) {
            var existing = stepRepository.findByRecipeIdAndCode(recipeId, candidate);
            if (existing.isEmpty() || (currentStepId != null && currentStepId.equals(existing.get().getId()))) {
                return candidate;
            }

            candidate = baseCode + "_" + suffix;
            suffix++;
        }
    }
}
