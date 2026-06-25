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
public class RecipeMatrixDto {
    private Long recipeId;
    private List<RecipeMatrixColumnDto> columns;
    private List<RecipeMatrixRowDto> rows;
}
