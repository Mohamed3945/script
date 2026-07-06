package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.StepKind;

/**
 * RecipeMatrixColumnDto class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeMatrixColumnDto {
    private Long stepId;
    private String stepCode;
    private String stepName;
    private StepKind stepKind;
    private Integer orderIndex;
}
