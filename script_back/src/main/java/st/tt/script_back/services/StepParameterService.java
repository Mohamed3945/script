package st.tt.script_back.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.StepParameterDto;
import st.tt.script_back.entities.ParameterDefinition;
import st.tt.script_back.entities.ParameterOption;
import st.tt.script_back.entities.Step;
import st.tt.script_back.entities.StepParameter;
import st.tt.script_back.enums.ActivationState;
import st.tt.script_back.enums.StepKind;
import st.tt.script_back.enums.StepType;
import st.tt.script_back.mappers.StepParameterMapper;
import st.tt.script_back.repositories.ParameterDefinitionRepository;
import st.tt.script_back.repositories.ParameterOptionRepository;
import st.tt.script_back.repositories.StepParameterRepository;
import st.tt.script_back.repositories.StepRepository;

/**
 * Handles lifecycle of step parameters and keeps activation graph consistent after each mutation.
 * <p>
 * Every create, update, or delete operation triggers recipe-level activation recalculation so matrix editability
 * reflects the latest rule configuration immediately.
 */
@Service
public class StepParameterService {

    private final StepParameterRepository stepParameterRepository;
    private final StepRepository stepRepository;
    private final ParameterDefinitionRepository parameterDefinitionRepository;
    private final ParameterOptionRepository parameterOptionRepository;
    private final StepParameterMapper stepParameterMapper;
    private final ParameterActivationService parameterActivationService;

    /**
     * Creates the service with repositories, mapper, and activation engine collaborators.
     *
     * @param stepParameterRepository persistence access for step parameters.
     * @param stepRepository persistence access for steps.
     * @param parameterDefinitionRepository persistence access for parameter definitions.
     * @param parameterOptionRepository persistence access for enum options.
     * @param stepParameterMapper DTO/entity mapper for step parameters.
     * @param parameterActivationService activation recalculation orchestrator.
     */
    public StepParameterService(
            StepParameterRepository stepParameterRepository,
            StepRepository stepRepository,
            ParameterDefinitionRepository parameterDefinitionRepository,
            ParameterOptionRepository parameterOptionRepository,
            StepParameterMapper stepParameterMapper,
            ParameterActivationService parameterActivationService) {
        this.stepParameterRepository = stepParameterRepository;
        this.stepRepository = stepRepository;
        this.parameterDefinitionRepository = parameterDefinitionRepository;
        this.parameterOptionRepository = parameterOptionRepository;
        this.stepParameterMapper = stepParameterMapper;
        this.parameterActivationService = parameterActivationService;
    }

    /**
     * Creates a new step parameter in a step and recalculates activation for the entire recipe.
     * <p>
     * Important rules enforced:
     * <ul>
     * <li>payload and definition id are mandatory,</li>
     * <li>parent parameter, when provided, must belong to the same step,</li>
     * <li>definition scope must match step kind (PRESTEP vs STEP),</li>
     * <li>new parameters start in {@link ActivationState#ENABLED} before recalculation.</li>
     * </ul>
     *
     * @param stepId target step identifier.
     * @param request create payload.
     * @return persisted parameter mapped as DTO.
     */
    @Transactional
    public StepParameterDto createStepParameter(Long stepId, StepParameterDto request) {
        if (request == null) {
            throw new IllegalArgumentException("StepParameter payload is required");
        }

        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException("Step with id " + stepId + " not found"));

        if (request.getDefinitionId() == null) {
            throw new IllegalArgumentException("definitionId is required");
        }

        ParameterDefinition definition = parameterDefinitionRepository.findById(request.getDefinitionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "ParameterDefinition with id " + request.getDefinitionId() + " not found"));

        validateDefinitionScope(step, definition);

        StepParameter parent = null;
        if (request.getParentStepParameterId() != null) {
            parent = stepParameterRepository.findById(request.getParentStepParameterId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Parent StepParameter with id " + request.getParentStepParameterId() + " not found"));
            if (parent.getStep() == null || !stepId.equals(parent.getStep().getId())) {
                throw new IllegalArgumentException("parentStepParameterId must belong to the same step");
            }
        }

        ParameterOption selectedOption = null;
        if (request.getSelectedOptionId() != null) {
            selectedOption = parameterOptionRepository.findById(request.getSelectedOptionId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "ParameterOption with id " + request.getSelectedOptionId() + " not found"));
        }

        StepParameter parameter = stepParameterMapper.toEntity(request);
        parameter.setStep(step);
        parameter.setDefinition(definition);
        parameter.setParentStepParameter(parent);
        parameter.setSelectedOption(selectedOption);
        parameter.setActivationState(ActivationState.ENABLED);

        if (parameter.getOrderIndex() == null) {
            Long scope = parent == null ? 0L : parent.getId();
            List<StepParameter> scoped = stepParameterRepository
                    .findByStepIdAndParentOrderScopeOrderByOrderIndexAsc(stepId, scope);
            int next = scoped.isEmpty() ? 0 : scoped.get(scoped.size() - 1).getOrderIndex() + 1;
            parameter.setOrderIndex(next);
        }

        StepParameter saved = stepParameterRepository.save(parameter);
        parameterActivationService.recalculateRecipeActivationStates(step.getRecipe().getId());
        return stepParameterMapper.toDto(saved);
    }

    /**
     * Executes getStepParameter.
     *
     * @param stepParameterId input argument consumed by getStepParameter.
     * @return computed StepParameterDto result returned by getStepParameter.
     */
    @Transactional(readOnly = true)
    public StepParameterDto getStepParameter(Long stepParameterId) {
        StepParameter parameter = stepParameterRepository.findById(stepParameterId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StepParameter with id " + stepParameterId + " not found"));
        return stepParameterMapper.toDto(parameter);
    }

    /**
     * Updates mutable fields of an existing step parameter and recalculates recipe activation.
     * <p>
     * Identity constraints are strict: {@code stepId} and {@code definitionId} cannot be changed through update.
     * Parent linkage is validated to remain in the same step.
     *
     * @param stepParameterId identifier of parameter to update.
     * @param request update payload.
     * @return updated parameter mapped as DTO.
     */
    @Transactional
    public StepParameterDto updateStepParameter(Long stepParameterId, StepParameterDto request) {
        if (request == null) {
            throw new IllegalArgumentException("StepParameter payload is required");
        }

        StepParameter existing = stepParameterRepository.findById(stepParameterId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StepParameter with id " + stepParameterId + " not found"));
        Long recipeId = existing.getStep() != null && existing.getStep().getRecipe() != null
                ? existing.getStep().getRecipe().getId()
                : null;

        if (request.getStepId() != null && existing.getStep() != null
                && !request.getStepId().equals(existing.getStep().getId())) {
            throw new IllegalArgumentException("stepId cannot be changed");
        }

        if (request.getDefinitionId() != null && existing.getDefinition() != null
                && !request.getDefinitionId().equals(existing.getDefinition().getId())) {
            throw new IllegalArgumentException("definitionId cannot be changed");
        }

        StepParameter parent = existing.getParentStepParameter();
        if (request.getParentStepParameterId() != null) {
            parent = stepParameterRepository.findById(request.getParentStepParameterId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Parent StepParameter with id " + request.getParentStepParameterId() + " not found"));
            if (parent.getStep() == null || existing.getStep() == null
                    || !existing.getStep().getId().equals(parent.getStep().getId())) {
                throw new IllegalArgumentException("parentStepParameterId must belong to the same step");
            }
        }

        ParameterOption selectedOption = null;
        if (request.getSelectedOptionId() != null) {
            selectedOption = parameterOptionRepository.findById(request.getSelectedOptionId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "ParameterOption with id " + request.getSelectedOptionId() + " not found"));
        }

        stepParameterMapper.updateEntityFromDto(request, existing);
        existing.setParentStepParameter(parent);
        existing.setSelectedOption(selectedOption);
        existing.setActivationState(ActivationState.ENABLED);
        if (existing.getStep() != null && existing.getDefinition() != null) {
            validateDefinitionScope(existing.getStep(), existing.getDefinition());
        }
        StepParameter saved = stepParameterRepository.save(existing);
        if (recipeId != null) {
            parameterActivationService.recalculateRecipeActivationStates(recipeId);
        }
        return stepParameterMapper.toDto(saved);
    }

    /**
     * Deletes a step parameter and recalculates activation states for remaining parameters in the recipe.
     *
     * @param stepParameterId identifier of parameter to delete.
     */
    @Transactional
    public void deleteStepParameter(Long stepParameterId) {
        StepParameter existing = stepParameterRepository.findById(stepParameterId)
            .orElseThrow(() -> new EntityNotFoundException(
                "StepParameter with id " + stepParameterId + " not found"));
        Long recipeId = existing.getStep() != null && existing.getStep().getRecipe() != null
                ? existing.getStep().getRecipe().getId()
                : null;
        stepParameterRepository.delete(existing);
        if (recipeId != null) {
            parameterActivationService.recalculateRecipeActivationStates(recipeId);
        }
    }

    /**
     * Validates compatibility between step kind and definition scope.
     * <p>
     * PRESTEP steps only accept PRESTEP definitions. Normal steps reject PRESTEP definitions.
     *
     * @param step owning step.
     * @param definition requested parameter definition.
     */
    private void validateDefinitionScope(Step step, ParameterDefinition definition) {
        if (step == null || definition == null || step.getStepKind() == null) {
            return;
        }

        StepType expected = step.getStepKind() == StepKind.PRESTEP ? StepType.PRESTEP : StepType.STEP;
        StepType actual = definition.getStepType() == null ? StepType.STEP : definition.getStepType();

        if (expected != actual) {
            throw new IllegalArgumentException(expected == StepType.PRESTEP
                    ? "Only PRESTEP parameter definitions are allowed for PRESTEP"
                    : "PRESTEP parameter definitions are not allowed for STEP");
        }
    }
}
