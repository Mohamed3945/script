package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DecisionExecutionAnswerDto {
    private Long questionId;
    private Long optionId;
    private Integer orderIndex;
}
