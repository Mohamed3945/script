package st.tt.script_back.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.ParameterValueType;

/**
 * RecipeMatrixRowDto class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeMatrixRowDto {
    private Long definitionId;
    private String parameterName;
    private String parameterAlias;
    private String parameterGroup;
    private Integer parameterGroupOrder;
    private ParameterValueType valueType;
    private List<RecipeMatrixCellDto> cells;
}
