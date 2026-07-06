package st.tt.script_back.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DecisionFinalizeRequestDto {
    private Long resultProfileId;
    private Long validatedGoldenRecipeId;
    private Long selectedMachineId;
    private Long creatorId;
    private List<DecisionExecutionAnswerDto> answers;
}
