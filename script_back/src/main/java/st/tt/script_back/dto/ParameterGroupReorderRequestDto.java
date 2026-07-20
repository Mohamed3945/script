package st.tt.script_back.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.StepType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParameterGroupReorderRequestDto {
    private StepType stepType;
    private List<Long> orderedGroupIds;
}