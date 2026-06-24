package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.RuleEffect;
import st.tt.script_back.enums.RuleScope;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDependencyRuleDto {
    private Long id;
    private Long sourceDefinitionId;
    private Long triggerOptionId;
    private Long targetDefinitionId;
    private RuleEffect effect;
    private RuleScope scope;
    private Integer priority;
}
