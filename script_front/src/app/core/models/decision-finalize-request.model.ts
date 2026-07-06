import { DecisionExecutionAnswer } from './decision-execution-answer.model';

export interface DecisionFinalizeRequest {
  resultProfileId: number;
  validatedGoldenRecipeId: number;
  selectedMachineId: number;
  creatorId: number;
  answers: DecisionExecutionAnswer[];
}
