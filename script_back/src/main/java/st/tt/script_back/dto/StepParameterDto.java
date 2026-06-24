package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.ActivationState;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StepParameterDto {
    private Long id;
    private Long stepId;
    private Long definitionId;
    private Long parentStepParameterId;
    private Long parentOrderScope;
    private Integer orderIndex;
    private String labelOverride;
    private String valueJson;
    private Long selectedOptionId;
    private ActivationState activationState;
    private boolean lockedByGolden;
}
