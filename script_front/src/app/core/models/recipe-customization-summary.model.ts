import { RecipeUntouchedModifiableItem } from './recipe-untouched-modifiable-item.model';

export interface RecipeCustomizationStats {
  modifiableCount: number;
  touchedCount: number;
  untouchedCount: number;
  computedExcludedCount: number;
  customizationRate: number;
}

export interface RecipeCustomizationSummary {
  recipeId: number;
  baselineRecipeId: number | null;
  workspaceType: string;
  stats: RecipeCustomizationStats;
  untouchedModifiableItems: RecipeUntouchedModifiableItem[];
}
