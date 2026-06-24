import { StepKind } from './step-kind.model';
import { StepParameter } from './step-parameter.model';

export interface StepDetail {
  id?: number;
  recipeId?: number;
  stepKind: StepKind;
  orderIndex?: number | null;
  name: string;
  parameters: StepParameter[];
}