import { RecipeConfigurationConstraint } from './recipe-configuration-constraint.model';

export interface RecipeCompatibleMachineSearchRequest {
  recipeId: number;
  capabilitySourceRecipeId?: number | null; 
  configurationConstraints: RecipeConfigurationConstraint[] | null;
}