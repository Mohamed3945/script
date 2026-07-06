package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ParameterOptionDto class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParameterOptionDto {
    private Long id;
    private Long definitionId;
    private String code;
    private String label;
    private Integer orderIndex;
}
