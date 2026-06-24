import { StepKind } from './step-kind.model';

export interface Step {
  id?: number;
  recipeId?: number;
  code?: string;
  stepKind: StepKind;
  orderIndex?: number | null;
  name: string;
}