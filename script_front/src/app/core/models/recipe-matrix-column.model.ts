import { StepKind } from './step-kind.model';

export interface RecipeMatrixColumn {
  stepId: number;
  stepCode: string;
  stepName: string;
  stepKind: StepKind;
  orderIndex: number;
}
