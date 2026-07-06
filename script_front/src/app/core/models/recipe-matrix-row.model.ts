import { ParameterValueType } from './parameter-value-type.model';
import { RecipeMatrixCell } from './recipe-matrix-cell.model';

export interface RecipeMatrixRow {
  definitionId: number;
  parameterName: string;
  parameterAlias: string;
  valueType: ParameterValueType;
  cells: RecipeMatrixCell[];
}
