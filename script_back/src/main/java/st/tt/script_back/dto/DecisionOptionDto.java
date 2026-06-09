package st.tt.script_back.dto;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;


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