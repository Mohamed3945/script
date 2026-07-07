package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.ActivationState;

/**
 * StepParameterDto class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StepParameterDto {
    private Long id;
    private Long stepId;
    private Long definitionId;
    private String definitionName;
    private String parameterGroup;
    private Integer parameterGroupOrder;
    private Long parentStepParameterId;
    private Long parentOrderScope;
    private Integer orderIndex;
    private String labelOverride;
    private String valueJson;
    private Long selectedOptionId;
    private String selectedOptionLabel;
    private ActivationState activationState;
    private boolean lockedByGolden;
    private boolean userModified;
    private boolean computed;
}
