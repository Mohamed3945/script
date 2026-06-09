package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DecisionQuestionDto {
    private Long id;
    private String code;
    private String label;
    private int orderIndex;
    private boolean entryPoint;
    private boolean active;
    private String questionType;
    private List<DecisionOptionDto> options;

}