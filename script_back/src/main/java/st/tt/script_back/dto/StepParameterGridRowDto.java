package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.ActivationState;
import st.tt.script_back.enums.ParameterValueType;
import st.tt.script_back.enums.StepKind;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StepParameterGridRowDto {
    private Long recipeId;
    private Long stepId;
    private String stepName;
    private StepKind stepKind;
    private String stepCode;
    private Integer stepOrderIndex;

    private Long stepParameterId;
    private Long parentStepParameterId;
    private Long parentOrderScope;
    private Integer parameterOrderIndex;

    private Long parameterDefinitionId;
    private String parameterDefinitionName;
    private ParameterValueType parameterValueType;

    private String labelOverride;
    private String valueJson;
    private Long selectedOptionId;
    private String selectedOptionLabel;

    private ActivationState activationState;
    private boolean lockedByGolden;
}
