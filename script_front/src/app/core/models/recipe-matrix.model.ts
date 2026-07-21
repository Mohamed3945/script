import { RecipeMatrixColumn } from './recipe-matrix-column.model';
import { RecipeMatrixEndpointCell } from './recipe-matrix-endpoint-cell.model';
import { RecipeMatrixRow } from './recipe-matrix-row.model';

export interface RecipeMatrix {
  recipeId: number;
  columns: RecipeMatrixColumn[];
  rows: RecipeMatrixRow[];
  endpointRow?: RecipeMatrixEndpointCell[];
}
