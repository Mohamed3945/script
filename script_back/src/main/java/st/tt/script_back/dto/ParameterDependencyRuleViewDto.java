package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.RuleEffect;
import st.tt.script_back.enums.RuleScope;

/**
 * ParameterDependencyRuleViewDto class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDependencyRuleViewDto {
    private Long id;

    private Long sourceDefinitionId;
    private String sourceDefinitionCode;
    private String sourceDefinitionName;

    private Long triggerOptionId;
    private String triggerOptionCode;
    private String triggerOptionLabel;

    private Long requiredSourceActivationOptionId;
    private String requiredSourceActivationOptionCode;
    private String requiredSourceActivationOptionLabel;

    private Long targetDefinitionId;
    private String targetDefinitionCode;
    private String targetDefinitionName;

    private RuleEffect effect;
    private RuleScope scope;
    private Integer priority;
}
