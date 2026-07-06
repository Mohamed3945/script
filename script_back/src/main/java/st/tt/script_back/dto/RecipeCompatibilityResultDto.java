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
public class RecipeCompatibilityResultDto {
    private Long recipeId;
    private int compatibleMachineCount;
    private int compatibleChamberCount;
    private List<CompatibleMachineDto> machines;
}
