import { RecipeConfigurationConstraint } from './recipe-configuration-constraint.model';

export interface RecipeCompatibleMachineSearchRequest {
  recipeId: number;
  configurationConstraints: RecipeConfigurationConstraint[];
}
