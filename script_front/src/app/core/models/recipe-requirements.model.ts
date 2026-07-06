import { RecipeRequiredCapability } from './recipe-required-capability.model';
import { RecipeRequiredConfiguration } from './recipe-required-configuration.model';

export interface RecipeRequirements {
  recipeId: number;
  recipeCode?: string | null;
  recipeName: string;
  requiredCapabilities: RecipeRequiredCapability[];
  requiredConfigurations: RecipeRequiredConfiguration[];
}
