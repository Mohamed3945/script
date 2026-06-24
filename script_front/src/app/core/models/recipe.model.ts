import { RecipeKind } from './recipe-kind.model';
import { RecipeStatus } from './recipe-status.model';

export interface Recipe {
  id?: number;
  recipeKind: RecipeKind;
  parentRecipeId?: number | null;
  name: string;
  description?: string | null;
  creatorId: number;
  revisorId?: number | null;
  processFamily?: string | null;
  status: RecipeStatus;
  version?: number | null;
  frozen: boolean;
}