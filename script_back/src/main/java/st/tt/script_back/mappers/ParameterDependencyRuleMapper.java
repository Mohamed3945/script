package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.ParameterDependencyRuleDto;
import st.tt.script_back.entities.ParameterDependencyRule;

@Component
public class ParameterDependencyRuleMapper {

    public ParameterDependencyRuleDto toDto(ParameterDependencyRule rule) {
        if (rule == null) {
            return null;
        }
        return new ParameterDependencyRuleDto(
                rule.getId(),
                rule.getSourceDefinition() != null ? rule.getSourceDefinition().getId() : null,
                rule.getTriggerOption() != null ? rule.getTriggerOption().getId() : null,
                rule.getTargetDefinition() != null ? rule.getTargetDefinition().getId() : null,
                rule.getEffect(),
                rule.getScope(),
                rule.getPriority()
        );
    }

    public List<ParameterDependencyRuleDto> toDtoList(List<ParameterDependencyRule> rules) {
        if (rules == null) {
            return Collections.emptyList();
        }
        return rules.stream().map(this::toDto).toList();
    }

    public ParameterDependencyRule toEntity(ParameterDependencyRuleDto dto) {
        if (dto == null) {
            return null;
        }
        ParameterDependencyRule entity = new ParameterDependencyRule();
        entity.setId(dto.getId());
        updateEntityFromDto(dto, entity);
        return entity;
    }

    public void updateEntityFromDto(ParameterDependencyRuleDto dto, ParameterDependencyRule entity) {
        entity.setEffect(dto.getEffect());
        entity.setScope(dto.getScope());
        entity.setPriority(dto.getPriority());
    }
}
