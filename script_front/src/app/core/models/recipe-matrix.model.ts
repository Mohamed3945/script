import { RecipeMatrixColumn } from './recipe-matrix-column.model';
import { RecipeMatrixRow } from './recipe-matrix-row.model';

export interface RecipeMatrix {
  recipeId: number;
  columns: RecipeMatrixColumn[];
  rows: RecipeMatrixRow[];
}
