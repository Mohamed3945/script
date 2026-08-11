package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FormulaReferenceDto {
    private Long id;
    private Integer slot;
    private String stepCode;
    private String definitionPath;
}