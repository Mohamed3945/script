package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import st.tt.script_back.enums.NextTransitionType;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DecisionNextResponseDto {

    private NextTransitionType type;
    private DecisionQuestionDto nextQuestion;
    private DecisionResultProfileDto resultProfile;
    
}
