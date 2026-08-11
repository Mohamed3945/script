import { FormulaReference } from './formula-reference.model';
import { RoundingPolicy } from './rounding-policy.model';

export interface ComputationFormula {
  id?: number;
  recipeId?: number;
  targetStepCode: string;
  targetDefinitionPath: string;
  expression: string;
  roundingMode: RoundingPolicy;
  decimals?: number | null;
  label?: string | null;
  references: FormulaReference[];
}
