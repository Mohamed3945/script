package st.tt.script_back.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.CreateStructuredStepRequestDto;
import st.tt.script_back.dto.StepDto;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.entities.Step;
import st.tt.script_back.entities.StepParameter;
import st.tt.script_back.enums.ActivationState;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.StepKind;
import st.tt.script_back.mappers.StepMapper;
import st.tt.script_back.repositories.RecipeRepository;
import st.tt.script_back.repositories.StepParameterRepository;
import st.tt.script_back.repositories.StepRepository;

@Service
public class RecipeStructureCommandService {

    private final RecipeRepository recipeRepository;
    private final StepRepository stepRepository;
    private final StepParameterRepository stepParameterRepository;
    private final StepMapper stepMapper;
    private final ParameterActivationService parameterActivationService;

    public RecipeStructureCommandService(
            RecipeRepository recipeRepository,
            StepRepository stepRepository,
            StepParameterRepository stepParameterRepository,
            StepMapper stepMapper,
            ParameterActivationService parameterActivationService) {
        this.recipeRepository = recipeRepository;
        this.stepRepository = stepRepository;
        this.stepParameterRepository = stepParameterRepository;
        this.stepMapper = stepMapper;
        this.parameterActivationService = parameterActivationService;
    }

    @Transactional
    public StepDto createStructuredStep(Long recipeId, CreateStructuredStepRequestDto request) {
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("CreateStructuredStep payload is required");
        }

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));

        assertRecipeAllowsStructuralChanges(recipe);

        StepKind stepKind = request.getStepKind() != null ? request.getStepKind() : StepKind.STEP;

        Step sourceStep = null;
        if (stepKind == StepKind.STEP) {
            sourceStep = stepRepository.findTopByRecipeIdAndStepKindOrderByOrderIndexDesc(recipeId, StepKind.STEP)
                    .orElse(null);
        } else if (stepKind == StepKind.PRESTEP) {
            sourceStep = stepRepository.findTopByRecipeIdAndStepKindOrderByOrderIndexDesc(recipeId, StepKind.PRESTEP)
                    .orElse(null);
        }

        Step newStep = new Step();
        newStep.setRecipe(recipe);
        newStep.setStepKind(stepKind);
        newStep.setName(resolveStepName(request, sourceStep, stepKind));

        Integer orderIndex = request.getOrderIndex() != null
                ? request.getOrderIndex()
                : computeNextOrderIndex(recipeId, stepKind);

        newStep.setOrderIndex(orderIndex);
        newStep.setCode(ensureUniqueCodeForRecipe(recipeId, orderIndex, null));

        Step savedStep = stepRepository.save(newStep);

        if (sourceStep != null) {
            cloneAllParametersFromSourceStep(sourceStep, savedStep);
        }

        parameterActivationService.recalculateRecipeActivationStates(recipeId);
        return stepMapper.toDto(savedStep);
    }

    private void cloneAllParametersFromSourceStep(Step sourceStep, Step targetStep) {
        List<StepParameter> sourceParameters = stepParameterRepository.findForStepClone(sourceStep.getId());
        if (sourceParameters.isEmpty()) {
            return;
        }

        Map<Long, StepParameter> clonedBySourceId = new HashMap<>();

        for (StepParameter source : sourceParameters.stream().filter(p -> p.getParentStepParameter() == null).toList()) {
            StepParameter saved = stepParameterRepository.save(buildClonedParameter(source, targetStep, null));
            clonedBySourceId.put(source.getId(), saved);
        }

        boolean progress = true;
        while (clonedBySourceId.size() < sourceParameters.size() && progress) {
            progress = false;

            for (StepParameter source : sourceParameters) {
                if (clonedBySourceId.containsKey(source.getId())) {
                    continue;
                }

                StepParameter sourceParent = source.getParentStepParameter();
                if (sourceParent == null) {
                    continue;
                }

                StepParameter clonedParent = clonedBySourceId.get(sourceParent.getId());
                if (clonedParent == null) {
                    continue;
                }

                StepParameter saved = stepParameterRepository.save(buildClonedParameter(source, targetStep, clonedParent));
                clonedBySourceId.put(source.getId(), saved);
                progress = true;
            }
        }

        if (clonedBySourceId.size() != sourceParameters.size()) {
            throw new IllegalStateException(
                    "Failed to clone full step parameter hierarchy from step " + sourceStep.getId());
        }
    }

    private StepParameter buildClonedParameter(StepParameter source, Step targetStep, StepParameter clonedParent) {
        StepParameter clone = new StepParameter();
        clone.setStep(targetStep);
        clone.setDefinition(source.getDefinition());
        clone.setParentStepParameter(clonedParent);
        clone.setOrderIndex(source.getOrderIndex());
        clone.setLabelOverride(source.getLabelOverride());
        clone.setValueJson(source.getValueJson());
        clone.setSelectedOption(source.getSelectedOption());
        clone.setActivationState(source.getActivationState() != null ? source.getActivationState() : ActivationState.ENABLED);
        clone.setLockedByGolden(source.isLockedByGolden());
        clone.setUserModified(false);
        return clone;
    }

    private String resolveStepName(CreateStructuredStepRequestDto request, Step sourceStep, StepKind stepKind) {
        if (request.getName() != null && !request.getName().isBlank()) {
            return request.getName();
        }
        if (sourceStep != null && sourceStep.getName() != null && !sourceStep.getName().isBlank()) {
            return sourceStep.getName();
        }
        return stepKind == StepKind.PRESTEP ? "PRESTEP" : "STEP";
    }

    private Integer computeNextOrderIndex(Long recipeId, StepKind stepKind) {
        if (stepKind == StepKind.PRESTEP) {
            return 0;
        }

        return stepRepository.findTopByRecipeIdAndStepKindOrderByOrderIndexDesc(recipeId, stepKind)
                .map(step -> step.getOrderIndex() == null ? 1 : step.getOrderIndex() + 1)
                .orElse(1);
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

    private void assertRecipeAllowsStructuralChanges(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe is required");
        }

        if (recipe.isFrozen()) {
            throw new IllegalStateException("Frozen recipe cannot be structurally modified");
        }

        if (recipe.getRecipeKind() == RecipeKind.DERIVED) {
            throw new IllegalStateException("Derived recipe cannot be structurally modified");
        }
    }
}