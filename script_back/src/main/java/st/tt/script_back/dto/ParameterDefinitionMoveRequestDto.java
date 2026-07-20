package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDefinitionMoveRequestDto {
    private Long definitionId;
    private Long targetGroupId;
    private Integer targetIndex;
}