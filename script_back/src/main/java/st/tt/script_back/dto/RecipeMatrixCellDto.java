package st.tt.script_back.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.ActivationState;
import st.tt.script_back.enums.ComputationStatus;
import st.tt.script_back.enums.ParameterValueType;

/**
 * RecipeMatrixCellDto class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeMatrixCellDto {
    private Long stepId;
    private Long stepParameterId;
    private Long definitionId;
    private ParameterValueType valueType;

    private String displayValue;
    private String valueJson;
    private Long selectedOptionId;
    private String selectedOptionLabel;

    private List<ParameterOptionDto> availableOptions;

    private ActivationState activationState;
    private boolean lockedByGolden;
    private boolean editable;
    private boolean userModified;
    private ComputationStatus computationStatus;
    private Instant computedAt;
    private boolean computedFromModified;
    private boolean computed;
}
