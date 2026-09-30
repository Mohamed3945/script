package st.tt.script_back.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DecisionTransitionAdminRequestDto {
    private Long currentQuestionId;
    private Long optionId;
    private Long nextQuestionId;
    private Long resultProfileId;
}