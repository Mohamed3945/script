import { ActivationState } from './activation-state.model';

export interface StepParameter {
  id?: number;
  stepId?: number;
  definitionId: number;
  parentStepParameterId?: number | null;
  parentOrderScope?: number | null;
  orderIndex?: number | null;
  labelOverride?: string | null;
  valueJson?: string | null;
  selectedOptionId?: number | null;
  activationState?: ActivationState;
  lockedByGolden: boolean;
}