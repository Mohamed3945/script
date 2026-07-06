package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.ParameterDependencyRuleDto;
import st.tt.script_back.entities.ParameterDependencyRule;

/**
 * ParameterDependencyRuleMapper class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Component
public class ParameterDependencyRuleMapper {

    /**
     * Executes toDto.
     *
     * @param rule input argument consumed by toDto.
     * @return computed ParameterDependencyRuleDto result returned by toDto.
     */
    public ParameterDependencyRuleDto toDto(ParameterDependencyRule rule) {
        if (rule == null) {
            return null;
        }
        return new ParameterDependencyRuleDto(
                rule.getId(),
                rule.getSourceDefinition() != null ? rule.getSourceDefinition().getId() : null,
                rule.getTriggerOption() != null ? rule.getTriggerOption().getId() : null,
            rule.getRequiredSourceActivationOption() != null
                ? rule.getRequiredSourceActivationOption().getId()
                : null,
                rule.getTargetDefinition() != null ? rule.getTargetDefinition().getId() : null,
                rule.getEffect(),
                rule.getScope(),
                rule.getPriority()
        );
    }

    /**
     * Executes toDtoList.
     *
     * @param rules input argument consumed by toDtoList.
     * @return computed List<ParameterDependencyRuleDto> result returned by toDtoList.
     */
    public List<ParameterDependencyRuleDto> toDtoList(List<ParameterDependencyRule> rules) {
        if (rules == null) {
            return Collections.emptyList();
        }
        return rules.stream().map(this::toDto).toList();
    }

    /**
     * Executes toEntity.
     *
     * @param dto input argument consumed by toEntity.
     * @return computed ParameterDependencyRule result returned by toEntity.
     */
    public ParameterDependencyRule toEntity(ParameterDependencyRuleDto dto) {
        if (dto == null) {
            return null;
        }
        ParameterDependencyRule entity = new ParameterDependencyRule();
        entity.setId(dto.getId());
        updateEntityFromDto(dto, entity);
        return entity;
    }

    /**
     * Executes updateEntityFromDto.
     *
     * @param dto input argument consumed by updateEntityFromDto.
     * @param entity input argument consumed by updateEntityFromDto.
     */
    public void updateEntityFromDto(ParameterDependencyRuleDto dto, ParameterDependencyRule entity) {
        entity.setEffect(dto.getEffect());
        entity.setScope(dto.getScope());
        entity.setPriority(dto.getPriority());
    }
}
