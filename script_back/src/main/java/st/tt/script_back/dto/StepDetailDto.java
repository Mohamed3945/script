package st.tt.script_back.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.StepKind;

/**
 * StepDetailDto class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StepDetailDto {
    private Long id;
    private Long recipeId;
    private StepKind stepKind;
    private Integer orderIndex;
    private String name;
    private List<StepParameterDto> parameters;
}
