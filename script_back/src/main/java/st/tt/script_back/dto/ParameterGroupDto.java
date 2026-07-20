package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.StepType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParameterGroupDto {
    private Long id;
    private String name;
    private StepType stepType;
    private Integer orderIndex;
}