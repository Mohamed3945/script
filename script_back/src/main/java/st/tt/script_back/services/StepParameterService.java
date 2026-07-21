package st.tt.script_back.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.StepParameterDto;
import st.tt.script_back.entities.ParameterDefinition;
import st.tt.script_back.entities.ParameterOption;
import st.tt.script_back.entities.Step;
import st.tt.script_back.entities.StepParameter;
import st.tt.script_back.enums.ActivationState;
import st.tt.script_back.enums.ParameterValueType;
import st.tt.script_back.enums.StepKind;
import st.tt.script_back.enums.ParameterScope;
import st.tt.script_back.mappers.StepParameterMapper;
import st.tt.script_back.repositories.ParameterDefinitionRepository;
import st.tt.script_back.repositories.ParameterOptionRepository;
import st.tt.script_back.repositories.RecipeRepository;
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
    private final RecipeRepository recipeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StepParameterService(
            StepParameterRepository stepParameterRepository,
            StepRepository stepRepository,
            RecipeRepository recipeRepository,
            ParameterDefinitionRepository parameterDefinitionRepository,
            ParameterOptionRepository parameterOptionRepository,
            StepParameterMapper stepParameterMapper,
            ParameterActivationService parameterActivationService) {
        this.stepParameterRepository = stepParameterRepository;
        this.stepRepository = stepRepository;
        this.recipeRepository = recipeRepository;
        this.parameterDefinitionRepository = parameterDefinitionRepository;
        this.parameterOptionRepository = parameterOptionRepository;
        this.stepParameterMapper = stepParameterMapper;
        this.parameterActivationService = parameterActivationService;
    }

    @Transactional
    public StepParameterDto createStepParameter(Long stepId, StepParameterDto request) {
        Step step = requireStep(stepId);
        StepParameter parameter = buildNewParameter(stepId, step, request);
        StepParameter saved = stepParameterRepository.save(parameter);

        parameterActivationService.recalculateRecipeActivationStates(step.getRecipe().getId());
        return stepParameterMapper.toDto(saved);
    }

    @Transactional
    public List<StepParameterDto> createRecipeWideStepParameter(
            Long recipeId,
            StepKind stepKind,
            StepParameterDto request) {

        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId is required");
        }
        if (stepKind == null) {
            throw new IllegalArgumentException("stepKind is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("StepParameter payload is required");
        }

        recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));

        List<Step> targets = stepRepository.findByRecipeIdAndStepKindOrderByOrderIndexAsc(recipeId, stepKind);

        if (targets.isEmpty()) {
            return List.of();
        }

        List<StepParameterDto> created = new ArrayList<>(targets.size());
        for (Step step : targets) {
            StepParameter parameter = buildNewParameter(step.getId(), step, request);
            StepParameter saved = stepParameterRepository.save(parameter);
            created.add(stepParameterMapper.toDto(saved));
        }

        parameterActivationService.recalculateRecipeActivationStates(recipeId);
        return created;
    }

    @Transactional
    public List<StepParameterDto> createStepParametersBulk(Long stepId, List<StepParameterDto> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("StepParameter payload list is required");
        }

        Step step = requireStep(stepId);
        List<StepParameterDto> result = new ArrayList<>(requests.size());

        for (StepParameterDto request : requests) {
            StepParameter parameter = buildNewParameter(stepId, step, request);
            StepParameter saved = stepParameterRepository.save(parameter);
            result.add(stepParameterMapper.toDto(saved));
        }

        parameterActivationService.recalculateRecipeActivationStates(step.getRecipe().getId());
        return result;
    }

    @Transactional(readOnly = true)
    public StepParameterDto getStepParameter(Long stepParameterId) {
        StepParameter parameter = stepParameterRepository.findById(stepParameterId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StepParameter with id " + stepParameterId + " not found"));
        return stepParameterMapper.toDto(parameter);
    }

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

        Long previousParentId = existing.getParentStepParameter() != null
                ? existing.getParentStepParameter().getId()
                : null;

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

        String previousValueJson = existing.getValueJson();
        Long previousSelectedOptionId = existing.getSelectedOption() != null ? existing.getSelectedOption().getId() : null;

        if (existing.getDefinition() != null) {
            request.setValueJson(normalizeJsonValue(request.getValueJson(), existing.getDefinition().getValueType()));
        }

        String requestedValueJson = request.getValueJson();
        Long requestedSelectedOptionId = request.getSelectedOptionId();

        boolean valueChanged = !Objects.equals(previousValueJson, requestedValueJson)
                || !Objects.equals(previousSelectedOptionId, requestedSelectedOptionId);

        Long nextParentId = parent != null ? parent.getId() : null;
        boolean parentChanged = !Objects.equals(previousParentId, nextParentId);

        stepParameterMapper.updateEntityFromDto(request, existing);
        existing.setParentStepParameter(parent);
        existing.setSelectedOption(selectedOption);

        if (valueChanged || parentChanged) {
            existing.setActivationState(ActivationState.ENABLED);
        }

        boolean isDerivedRecipe =
                existing.getStep() != null
                && existing.getStep().getRecipe() != null
                && existing.getStep().getRecipe().getRecipeKind() == st.tt.script_back.enums.RecipeKind.DERIVED;

        if (valueChanged) {
            existing.setUserModified(isDerivedRecipe);
        } else if (!isDerivedRecipe) {
            existing.setUserModified(false);
        }

        if (existing.getStep() != null && existing.getDefinition() != null) {
            validateDefinitionScope(existing.getStep(), existing.getDefinition());
        }

        StepParameter saved = stepParameterRepository.save(existing);

        if (recipeId != null && (valueChanged || parentChanged)) {
            parameterActivationService.recalculateRecipeActivationStates(recipeId);
        }

        return stepParameterMapper.toDto(saved);
    }

    @Transactional
    public void deleteRecipeWideStepParameter(Long recipeId, Long definitionId) {
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId is required");
        }
        if (definitionId == null) {
            throw new IllegalArgumentException("definitionId is required");
        }

        recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));

        int deletedCount = stepParameterRepository.deleteByRecipeIdAndDefinitionId(recipeId, definitionId);

        if (deletedCount == 0) {
            throw new EntityNotFoundException(
                    "No step parameters found for recipeId=" + recipeId + " and definitionId=" + definitionId);
        }

        parameterActivationService.recalculateRecipeActivationStates(recipeId);
    }

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

    private Step requireStep(Long stepId) {
        return stepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException("Step with id " + stepId + " not found"));
    }

    private StepParameter buildNewParameter(Long stepId, Step step, StepParameterDto request) {
        if (request == null) {
            throw new IllegalArgumentException("StepParameter payload is required");
        }

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

        request.setValueJson(normalizeJsonValue(request.getValueJson(), definition.getValueType()));

        StepParameter parameter = stepParameterMapper.toEntity(request);
        parameter.setStep(step);
        parameter.setDefinition(definition);
        parameter.setParentStepParameter(parent);
        parameter.setSelectedOption(selectedOption);
        parameter.setActivationState(ActivationState.ENABLED);
        parameter.setUserModified(false);

        if (parameter.getOrderIndex() == null) {
            Long scope = parent == null ? 0L : parent.getId();
            List<StepParameter> scoped = stepParameterRepository
                    .findByStepIdAndParentOrderScopeOrderByOrderIndexAsc(stepId, scope);
            int next = scoped.isEmpty() ? 0 : scoped.get(scoped.size() - 1).getOrderIndex() + 1;
            parameter.setOrderIndex(next);
        }

        return parameter;
    }

    private String normalizeJsonValue(String rawValue, ParameterValueType valueType) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        if (valueType == null) {
            return rawValue;
        }

        String trimmed = rawValue.trim();

        try {
            switch (valueType) {
                case STRING:
                    if (isJsonStringLiteral(trimmed)) {
                        return trimmed;
                    }
                    return objectMapper.writeValueAsString(trimmed);

                case NUMBER:
                    JsonNode numberNode = objectMapper.readTree(trimmed);
                    if (numberNode.isNumber()) {
                        return numberNode.toString();
                    }
                    // Accept legacy JSON string numbers: "12", "12.5"
                    if (numberNode.isTextual()) {
                        String text = numberNode.asText().trim().replace(',', '.');
                        try {
                            return new BigDecimal(text).stripTrailingZeros().toPlainString();
                        } catch (NumberFormatException ex) {
                            throw new IllegalArgumentException("Parameter value must be a valid number");
                        }
                    }
                    throw new IllegalArgumentException("Parameter value must be a valid number");

                case BOOLEAN:
                    JsonNode booleanNode = objectMapper.readTree(trimmed);
                    if (!booleanNode.isBoolean()) {
                        throw new IllegalArgumentException("Parameter value must be true or false");
                    }
                    return booleanNode.toString();

                case JSON:
                    objectMapper.readTree(trimmed);
                    return trimmed;

                case ENUM:
                    return null;

                default:
                    return trimmed;
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Invalid parameter value for valueType " + valueType + ": " + ex.getMessage(), ex);
        }
    }

    private boolean isJsonStringLiteral(String value) {
        try {
            JsonNode node = objectMapper.readTree(value);
            return node.isTextual();
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Validates compatibility between step kind and definition scope.
     */
    private void validateDefinitionScope(Step step, ParameterDefinition definition) {
        if (step == null || definition == null || step.getStepKind() == null) {
            return;
        }

        ParameterScope expected = step.getStepKind() == StepKind.PRESTEP
            ? ParameterScope.PRESTEP
            : ParameterScope.STEP;
        ParameterScope actual = definition.getStepType() == null ? ParameterScope.STEP : definition.getStepType();

        if (expected != actual) {
            throw new IllegalArgumentException(expected == ParameterScope.PRESTEP
                    ? "Only PRESTEP parameter definitions are allowed for PRESTEP"
                    : "PRESTEP parameter definitions are not allowed for STEP");
        }
    }
}
