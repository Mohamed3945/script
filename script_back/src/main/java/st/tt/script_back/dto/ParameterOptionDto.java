package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParameterOptionDto {
    private Long id;
    private Long definitionId;
    private String code;
    private String label;
    private Integer orderIndex;
}
