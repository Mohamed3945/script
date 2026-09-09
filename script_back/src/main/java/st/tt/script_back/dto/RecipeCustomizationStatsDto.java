package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stats de customisation pour une recette derivee.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeCustomizationStatsDto {
    private int modifiableCount;
    private int touchedCount;
    private int untouchedCount;
    private int computedExcludedCount;
    private double customizationRate;
}
