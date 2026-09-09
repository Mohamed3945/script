package st.tt.script_back.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.StepEndpointConditionDto;
import st.tt.script_back.dto.StepEndpointDto;
import st.tt.script_back.entities.ParameterDefinition;
import st.tt.script_back.entities.ParameterOption;
import st.tt.script_back.entities.Step;
import st.tt.script_back.entities.StepEndpoint;
import st.tt.script_back.entities.StepEndpointCondition;
import st.tt.script_back.enums.ParameterScope;
import st.tt.script_back.repositories.ParameterDefinitionRepository;
import st.tt.script_back.repositories.ParameterOptionRepository;
import st.tt.script_back.repositories.StepEndpointRepository;
import st.tt.script_back.repositories.StepRepository;

@Service
public class StepEndpointService {

    private final StepEndpointRepository stepEndpointRepository;
    private final StepRepository stepRepository;
    private final ParameterDefinitionRepository parameterDefinitionRepository;
    private final ParameterOptionRepository parameterOptionRepository;

    public StepEndpointService(
            StepEndpointRepository stepEndpointRepository,
            StepRepository stepRepository,
            ParameterDefinitionRepository parameterDefinitionRepository,
            ParameterOptionRepository parameterOptionRepository) {
        this.stepEndpointRepository = stepEndpointRepository;
        this.stepRepository = stepRepository;
        this.parameterDefinitionRepository = parameterDefinitionRepository;
        this.parameterOptionRepository = parameterOptionRepository;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET — charge l'endpoint d'un step
    // Retourne un DTO vide (clause=null, conditions=[]) si aucun endpoint
    // n'existe encore en base pour ce step
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public StepEndpointDto getEndpoint(Long stepId) {
        ensureStepExists(stepId);
        return stepEndpointRepository
                .findByStepIdWithConditions(stepId)
                .map(this::toDto)
                .orElse(emptyDto(stepId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPSERT — crée ou remplace intégralement l'endpoint du step
    // Si le payload a 0 conditions → on persiste un endpoint vide
    // (clause=null), ce qui génèrera <ENDPOINT /> à l'export XML
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public StepEndpointDto upsertEndpoint(Long stepId, StepEndpointDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Step with id " + stepId + " not found"));

        // Charger ou créer l'endpoint
        StepEndpoint endpoint = stepEndpointRepository
                .findByStepIdWithConditions(stepId)
                .orElseGet(() -> {
                    StepEndpoint e = new StepEndpoint();
                    e.setStep(step);
                    return e;
                });

        boolean hasConditions = request.getConditions() != null
                && !request.getConditions().isEmpty();

        if (request.getLockedByGolden() != null) {
            endpoint.setLockedByGolden(Boolean.TRUE.equals(request.getLockedByGolden()));
        }

        // clause cohérente avec les conditions
        endpoint.setClause(hasConditions ? request.getClause() : null);

        // Reconstruction complète des conditions.
        // Important: flush après clear pour exécuter les DELETE avant les INSERT,
        // sinon la contrainte unique (endpoint_id, order_index) peut échouer.
        endpoint.getConditions().clear();
        stepEndpointRepository.flush();

        if (hasConditions) {
            List<StepEndpointConditionDto> condDtos = request.getConditions();
            Set<Long> usedEndpointParameterIds = new HashSet<>();
            for (int i = 0; i < condDtos.size(); i++) {
                StepEndpointConditionDto dto = condDtos.get(i);

                // Validation : endpointParameterId obligatoire
                if (dto.getEndpointParameterId() == null) {
                    throw new IllegalArgumentException(
                            "endpointParameterId is required for each condition");
                }

                if (!usedEndpointParameterIds.add(dto.getEndpointParameterId())) {
                    throw new IllegalArgumentException(
                            "Duplicate endpointParameterId is not allowed in endpoint conditions"
                            + " (condition index " + i + ")");
                }

                // Validation : operator obligatoire
                if (dto.getOperator() == null) {
                    throw new IllegalArgumentException(
                            "operator is required for each condition");
                }

                // Validation : exactement une valeur
                if ((dto.getValueJson() == null) == (dto.getSelectedOptionId() == null)) {
                    throw new IllegalArgumentException(
                            "Exactly one of valueJson or selectedOptionId must be set"
                            + " (condition index " + i + ")");
                }

                ParameterDefinition endpointParam = parameterDefinitionRepository
                        .findById(dto.getEndpointParameterId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "ParameterDefinition with id "
                                + dto.getEndpointParameterId() + " not found"));

                // Garde : le paramètre doit être de scope ENDPOINT
                if (endpointParam.getStepType() != ParameterScope.ENDPOINT) {
                    throw new IllegalArgumentException(
                            "ParameterDefinition " + dto.getEndpointParameterId()
                            + " is not of scope ENDPOINT");
                }

                ParameterOption selectedOption = null;
                if (dto.getSelectedOptionId() != null) {
                    selectedOption = parameterOptionRepository
                            .findById(dto.getSelectedOptionId())
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "ParameterOption with id "
                                    + dto.getSelectedOptionId() + " not found"));
                }

                StepEndpointCondition condition = new StepEndpointCondition();
                condition.setEndpoint(endpoint);
                condition.setEndpointParameter(endpointParam);
                condition.setValueJson(dto.getValueJson());
                condition.setSelectedOption(selectedOption);
                condition.setOperator(dto.getOperator());
                condition.setOrderIndex(i); // on ignore l'orderIndex du DTO et on recalcule

                endpoint.getConditions().add(condition);
            }
        }

        StepEndpoint saved = stepEndpointRepository.save(endpoint);
        return toDto(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE — supprime l'endpoint du step
    // Après suppression, l'export XML génèrera <ENDPOINT /> (time-based)
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public void deleteEndpoint(Long stepId) {
        ensureStepExists(stepId);
        StepEndpoint endpoint = stepEndpointRepository
                .findByStepIdWithConditions(stepId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No endpoint found for step id " + stepId));
        stepEndpointRepository.delete(endpoint);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private StepEndpointDto toDto(StepEndpoint e) {
        List<StepEndpointConditionDto> condDtos = e.getConditions() == null
                ? List.of()
                : e.getConditions().stream()
                        .map(this::toConditionDto)
                        .toList();
        return new StepEndpointDto(e.getId(), e.getStep().getId(), e.getClause(), e.isLockedByGolden(), condDtos);
    }

    private StepEndpointConditionDto toConditionDto(StepEndpointCondition c) {
        StepEndpointConditionDto dto = new StepEndpointConditionDto();
        dto.setId(c.getId());
        dto.setOperator(c.getOperator());
        dto.setValueJson(c.getValueJson());
        dto.setOrderIndex(c.getOrderIndex());

        // Dénormalisation du paramètre endpoint
        if (c.getEndpointParameter() != null) {
            ParameterDefinition ep = c.getEndpointParameter();
            dto.setEndpointParameterId(ep.getId());
            dto.setEndpointParameterAlias(ep.getAlias());
            dto.setEndpointParameterName(ep.getName());
            dto.setEndpointParameterUnit(ep.getUnit());
            dto.setEndpointParameterValueType(ep.getValueType());
        }

        // Dénormalisation de l'option sélectionnée
        if (c.getSelectedOption() != null) {
            dto.setSelectedOptionId(c.getSelectedOption().getId());
            dto.setSelectedOptionLabel(c.getSelectedOption().getLabel());
        }

        return dto;
    }

    private StepEndpointDto emptyDto(Long stepId) {
        return new StepEndpointDto(null, stepId, null, false, List.of());
    }

    private void ensureStepExists(Long stepId) {
        if (!stepRepository.existsById(stepId)) {
            throw new EntityNotFoundException("Step with id " + stepId + " not found");
        }
    }
}