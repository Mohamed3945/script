package st.tt.script_back.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.ParameterValueType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeMatrixRowDto {
    private Long definitionId;
    private String parameterName;
    private String parameterAlias;
    private ParameterValueType valueType;
    private List<RecipeMatrixCellDto> cells;
}
