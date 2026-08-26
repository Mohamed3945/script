package st.tt.script_back.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bilan de customisation pour la review avant export XML.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeCustomizationSummaryDto {
    private Long recipeId;
    private Long baselineRecipeId;
    private String workspaceType;
    private RecipeCustomizationStatsDto stats;
    private List<RecipeUntouchedModifiableItemDto> untouchedModifiableItems;
}
