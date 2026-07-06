package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.ParameterDependencyRuleViewDto;
import st.tt.script_back.entities.ParameterDefinition;
import st.tt.script_back.entities.ParameterDependencyRule;
import st.tt.script_back.entities.ParameterOption;

/**
 * ParameterDependencyRuleViewMapper class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Component
public class ParameterDependencyRuleViewMapper {

    /**
     * Executes toDto.
     *
     * @param rule input argument consumed by toDto.
     * @return computed ParameterDependencyRuleViewDto result returned by toDto.
     */
    public ParameterDependencyRuleViewDto toDto(ParameterDependencyRule rule) {
        if (rule == null) {
            return null;
        }

        ParameterDefinition source = rule.getSourceDefinition();
        ParameterOption trigger = rule.getTriggerOption();
        ParameterOption required = rule.getRequiredSourceActivationOption();
        ParameterDefinition target = rule.getTargetDefinition();

        return new ParameterDependencyRuleViewDto(
                rule.getId(),
                source != null ? source.getId() : null,
                source != null ? source.getCode() : null,
                source != null ? source.getName() : null,
                trigger != null ? trigger.getId() : null,
                trigger != null ? trigger.getCode() : null,
                trigger != null ? trigger.getLabel() : null,
                required != null ? required.getId() : null,
                required != null ? required.getCode() : null,
                required != null ? required.getLabel() : null,
                target != null ? target.getId() : null,
                target != null ? target.getCode() : null,
                target != null ? target.getName() : null,
                rule.getEffect(),
                rule.getScope(),
                rule.getPriority());
    }

    /**
     * Executes toDtoList.
     *
     * @param rules input argument consumed by toDtoList.
     * @return computed List<ParameterDependencyRuleViewDto> result returned by toDtoList.
     */
    public List<ParameterDependencyRuleViewDto> toDtoList(List<ParameterDependencyRule> rules) {
        if (rules == null) {
            return Collections.emptyList();
        }
        return rules.stream().map(this::toDto).toList();
    }
}
