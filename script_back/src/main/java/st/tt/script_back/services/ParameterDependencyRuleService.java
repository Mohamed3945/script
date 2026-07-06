package st.tt.script_back.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.ParameterDependencyRuleDto;
import st.tt.script_back.entities.ParameterDefinition;
import st.tt.script_back.entities.ParameterDependencyRule;
import st.tt.script_back.entities.ParameterOption;
import st.tt.script_back.mappers.ParameterDependencyRuleMapper;
import st.tt.script_back.repositories.ParameterDefinitionRepository;
import st.tt.script_back.repositories.ParameterDependencyRuleRepository;
import st.tt.script_back.repositories.ParameterOptionRepository;
import st.tt.script_back.repositories.StepParameterRepository;

/**
 * ParameterDependencyRuleService class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Service
public class ParameterDependencyRuleService {

    private final ParameterDependencyRuleRepository parameterDependencyRuleRepository;
    private final ParameterDefinitionRepository parameterDefinitionRepository;
    private final ParameterOptionRepository parameterOptionRepository;
    private final ParameterDependencyRuleMapper parameterDependencyRuleMapper;
    private final ParameterActivationService parameterActivationService;
    private final StepParameterRepository stepParameterRepository;

    /**
     * Executes ParameterDependencyRuleService.
     *
     * @param parameterDependencyRuleRepository input argument consumed by ParameterDependencyRuleService.
     * @param parameterDefinitionRepository input argument consumed by ParameterDependencyRuleService.
     * @param parameterOptionRepository input argument consumed by ParameterDependencyRuleService.
     * @param parameterDependencyRuleMapper input argument consumed by ParameterDependencyRuleService.
     * @param parameterActivationService input argument consumed by ParameterDependencyRuleService.
     * @param stepParameterRepository input argument consumed by ParameterDependencyRuleService.
     */
    public ParameterDependencyRuleService(
            ParameterDependencyRuleRepository parameterDependencyRuleRepository,
            ParameterDefinitionRepository parameterDefinitionRepository,
            ParameterOptionRepository parameterOptionRepository,
            ParameterDependencyRuleMapper parameterDependencyRuleMapper,
            ParameterActivationService parameterActivationService,
            StepParameterRepository stepParameterRepository) {
        this.parameterDependencyRuleRepository = parameterDependencyRuleRepository;
        this.parameterDefinitionRepository = parameterDefinitionRepository;
        this.parameterOptionRepository = parameterOptionRepository;
        this.parameterDependencyRuleMapper = parameterDependencyRuleMapper;
        this.parameterActivationService = parameterActivationService;
        this.stepParameterRepository = stepParameterRepository;
    }

    /**
     * Executes createParameterDependencyRule.
     *
     * @param request input argument consumed by createParameterDependencyRule.
     * @return computed ParameterDependencyRuleDto result returned by createParameterDependencyRule.
     */
    @Transactional
    public ParameterDependencyRuleDto createParameterDependencyRule(ParameterDependencyRuleDto request) {
        if (request == null) {
            throw new IllegalArgumentException("ParameterDependencyRule payload is required");
        }
        if (request.getSourceDefinitionId() == null || request.getTargetDefinitionId() == null
                || request.getTriggerOptionId() == null) {
            throw new IllegalArgumentException(
                    "sourceDefinitionId, triggerOptionId and targetDefinitionId are required");
        }

        ParameterDefinition source = parameterDefinitionRepository.findById(request.getSourceDefinitionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Source ParameterDefinition with id " + request.getSourceDefinitionId() + " not found"));
        ParameterDefinition target = parameterDefinitionRepository.findById(request.getTargetDefinitionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Target ParameterDefinition with id " + request.getTargetDefinitionId() + " not found"));
        ParameterOption triggerOption = parameterOptionRepository.findById(request.getTriggerOptionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trigger ParameterOption with id " + request.getTriggerOptionId() + " not found"));
        ParameterOption requiredSourceActivationOption = null;
        if (request.getRequiredSourceActivationOptionId() != null) {
            requiredSourceActivationOption = parameterOptionRepository.findById(request.getRequiredSourceActivationOptionId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Required source activation ParameterOption with id "
                        + request.getRequiredSourceActivationOptionId() + " not found"));
        }

        ParameterDependencyRule rule = parameterDependencyRuleMapper.toEntity(request);
        rule.setSourceDefinition(source);
        rule.setTargetDefinition(target);
        rule.setTriggerOption(triggerOption);
        rule.setRequiredSourceActivationOption(requiredSourceActivationOption);

        if (rule.getPriority() == null) {
            rule.setPriority(0);
        }

        ParameterDependencyRule saved = parameterDependencyRuleRepository.save(rule);
        recalculateAffectedRecipes(source.getId(), target.getId());
        return parameterDependencyRuleMapper.toDto(saved);
    }

    /**
     * Executes getParameterDependencyRules.
     * @return computed List<ParameterDependencyRuleDto> result returned by getParameterDependencyRules.
     */
    @Transactional(readOnly = true)
    public List<ParameterDependencyRuleDto> getParameterDependencyRules() {
        return parameterDependencyRuleMapper.toDtoList(
                parameterDependencyRuleRepository.findAll(Sort.by(Sort.Direction.ASC, "priority").ascending()
                        .and(Sort.by(Sort.Direction.ASC, "id"))));
    }

    /**
     * Executes getParameterDependencyRule.
     *
     * @param id input argument consumed by getParameterDependencyRule.
     * @return computed ParameterDependencyRuleDto result returned by getParameterDependencyRule.
     */
    @Transactional(readOnly = true)
    public ParameterDependencyRuleDto getParameterDependencyRule(Long id) {
        ParameterDependencyRule rule = parameterDependencyRuleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "ParameterDependencyRule with id " + id + " not found"));
        return parameterDependencyRuleMapper.toDto(rule);
    }

    /**
     * Executes updateParameterDependencyRule.
     *
     * @param id input argument consumed by updateParameterDependencyRule.
     * @param request input argument consumed by updateParameterDependencyRule.
     * @return computed ParameterDependencyRuleDto result returned by updateParameterDependencyRule.
     */
    @Transactional
    public ParameterDependencyRuleDto updateParameterDependencyRule(Long id, ParameterDependencyRuleDto request) {
        if (request == null) {
            throw new IllegalArgumentException("ParameterDependencyRule payload is required");
        }

        ParameterDependencyRule existing = parameterDependencyRuleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "ParameterDependencyRule with id " + id + " not found"));

        Long previousSourceDefinitionId = existing.getSourceDefinition() != null ? existing.getSourceDefinition().getId() : null;
        Long previousTargetDefinitionId = existing.getTargetDefinition() != null ? existing.getTargetDefinition().getId() : null;

        if (request.getSourceDefinitionId() == null || request.getTargetDefinitionId() == null
                || request.getTriggerOptionId() == null) {
            throw new IllegalArgumentException(
                    "sourceDefinitionId, triggerOptionId and targetDefinitionId are required");
        }

        ParameterDefinition source = parameterDefinitionRepository.findById(request.getSourceDefinitionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Source ParameterDefinition with id " + request.getSourceDefinitionId() + " not found"));
        ParameterDefinition target = parameterDefinitionRepository.findById(request.getTargetDefinitionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Target ParameterDefinition with id " + request.getTargetDefinitionId() + " not found"));
        ParameterOption triggerOption = parameterOptionRepository.findById(request.getTriggerOptionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trigger ParameterOption with id " + request.getTriggerOptionId() + " not found"));
        ParameterOption requiredSourceActivationOption = null;
        if (request.getRequiredSourceActivationOptionId() != null) {
            requiredSourceActivationOption = parameterOptionRepository.findById(request.getRequiredSourceActivationOptionId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Required source activation ParameterOption with id "
                        + request.getRequiredSourceActivationOptionId() + " not found"));
        }

        parameterDependencyRuleMapper.updateEntityFromDto(request, existing);
        existing.setSourceDefinition(source);
        existing.setTargetDefinition(target);
        existing.setTriggerOption(triggerOption);
        existing.setRequiredSourceActivationOption(requiredSourceActivationOption);
        ParameterDependencyRule saved = parameterDependencyRuleRepository.save(existing);
        recalculateAffectedRecipes(previousSourceDefinitionId, previousTargetDefinitionId, source.getId(), target.getId());
        return parameterDependencyRuleMapper.toDto(saved);
    }

    /**
     * Executes deleteParameterDependencyRule.
     *
     * @param id input argument consumed by deleteParameterDependencyRule.
     */
    @Transactional
    public void deleteParameterDependencyRule(Long id) {
        ParameterDependencyRule existing = parameterDependencyRuleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "ParameterDependencyRule with id " + id + " not found"));
        Long sourceDefinitionId = existing.getSourceDefinition() != null ? existing.getSourceDefinition().getId() : null;
        Long targetDefinitionId = existing.getTargetDefinition() != null ? existing.getTargetDefinition().getId() : null;
        parameterDependencyRuleRepository.delete(existing);
        recalculateAffectedRecipes(sourceDefinitionId, targetDefinitionId);
    }

    private void recalculateAffectedRecipes(Long... definitionIds) {
        List<Long> nonNullDefinitionIds = new ArrayList<>();
        for (Long definitionId : definitionIds) {
            if (definitionId != null && !nonNullDefinitionIds.contains(definitionId)) {
                nonNullDefinitionIds.add(definitionId);
            }
        }
        if (nonNullDefinitionIds.isEmpty()) {
            return;
        }

        List<Long> recipeIds = stepParameterRepository.findDistinctRecipeIdsByDefinitionIds(nonNullDefinitionIds);
        recipeIds.forEach(parameterActivationService::recalculateRecipeActivationStates);
    }
}
