import { CompatibleMachine } from './compatible-machine.model';

export interface RecipeCompatibilityResult {
  recipeId: number;
  compatibleMachineCount: number;
  compatibleChamberCount: number;
  machines: CompatibleMachine[];
}
