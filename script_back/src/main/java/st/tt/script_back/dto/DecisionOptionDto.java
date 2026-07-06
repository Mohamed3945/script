package st.tt.script_back.dto;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * DecisionOptionDto class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DecisionOptionDto {
    private Long id;
    private String label;
    private String value;
    private int orderIndex;


}
