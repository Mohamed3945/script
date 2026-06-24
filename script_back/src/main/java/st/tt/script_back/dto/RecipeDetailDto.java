package st.tt.script_back.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.RecipeStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDetailDto {
    private Long id;
    private RecipeKind recipeKind;
    private Long parentRecipeId;
    private String name;
    private String description;
    private Long creatorId;
    private Long revisorId;
    private String processFamily;
    private RecipeStatus status;
    private Integer version;
    private boolean frozen;
    private List<StepDto> steps;
}
