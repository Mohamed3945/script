package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import st.tt.script_back.enums.NextTransitionType;


/**
 * DecisionNextResponseDto class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DecisionNextResponseDto {

    private NextTransitionType type;
    private DecisionQuestionDto nextQuestion;
    private DecisionResultProfileDto resultProfile;
    
}
