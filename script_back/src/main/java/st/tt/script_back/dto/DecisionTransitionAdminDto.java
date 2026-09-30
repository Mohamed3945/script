package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DecisionTransitionAdminDto {
    private Long id;
    private Long currentQuestionId;
    private String currentQuestionCode;
    private String currentQuestionLabel;
    private Long optionId;
    private String optionLabel;
    private Long nextQuestionId;
    private String nextQuestionCode;
    private Long resultProfileId;
    private String resultProfileCode;
}