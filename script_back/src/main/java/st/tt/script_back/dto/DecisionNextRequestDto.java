package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DecisionNextRequestDto class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DecisionNextRequestDto {
    private Long currentQuestionId;
    private Long selectedOptionId;
}
