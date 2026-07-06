import { RecipeKind } from '../models/recipe-kind.model';

export function buildRecipeDetailRouteByKind(recipeId: number, recipeKind: RecipeKind): (string | number)[] {
  return recipeKind === 'GOLDEN'
    ? ['/recipes', 'golden', recipeId]
    : ['/recipes', 'derived', recipeId];
}
