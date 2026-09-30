package st.tt.script_back.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DecisionQuestionAdminRequestDto {
    private String code;
    private String label;
    private String questionType;
    private boolean entryPoint;
    private int orderIndex;
    private boolean active;
}