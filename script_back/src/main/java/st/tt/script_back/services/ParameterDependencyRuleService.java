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

@Service
public class ParameterDependencyRuleService {

    private final ParameterDependencyRuleRepository parameterDependencyRuleRepository;
    private final ParameterDefinitionRepository parameterDefinitionRepository;
    private final ParameterOptionRepository parameterOptionRepository;
    private final ParameterDependencyRuleMapper parameterDependencyRuleMapper;
    private final ParameterActivationService parameterActivationService;
    private final StepParameterRepository stepParameterRepository;

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

        ParameterDependencyRule rule = parameterDependencyRuleMapper.toEntity(request);
        rule.setSourceDefinition(source);
        rule.setTargetDefinition(target);
        rule.setTriggerOption(triggerOption);

        if (rule.getPriority() == null) {
            rule.setPriority(0);
        }

        ParameterDependencyRule saved = parameterDependencyRuleRepository.save(rule);
        recalculateAffectedRecipes(source.getId(), target.getId());
        return parameterDependencyRuleMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ParameterDependencyRuleDto> getParameterDependencyRules() {
        return parameterDependencyRuleMapper.toDtoList(
                parameterDependencyRuleRepository.findAll(Sort.by(Sort.Direction.ASC, "priority").ascending()
                        .and(Sort.by(Sort.Direction.ASC, "id"))));
    }

    @Transactional(readOnly = true)
    public ParameterDependencyRuleDto getParameterDependencyRule(Long id) {
        ParameterDependencyRule rule = parameterDependencyRuleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "ParameterDependencyRule with id " + id + " not found"));
        return parameterDependencyRuleMapper.toDto(rule);
    }

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

        parameterDependencyRuleMapper.updateEntityFromDto(request, existing);
        existing.setSourceDefinition(source);
        existing.setTargetDefinition(target);
        existing.setTriggerOption(triggerOption);
        ParameterDependencyRule saved = parameterDependencyRuleRepository.save(existing);
        recalculateAffectedRecipes(previousSourceDefinitionId, previousTargetDefinitionId, source.getId(), target.getId());
        return parameterDependencyRuleMapper.toDto(saved);
    }

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
