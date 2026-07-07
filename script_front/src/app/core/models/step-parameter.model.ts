import { ActivationState } from './activation-state.model';

export interface StepParameter {
  id?: number;
  stepId?: number;
  definitionId: number;
  definitionName?: string | null;
  parameterGroup?: string | null;
  parameterGroupOrder?: number | null;
  parentStepParameterId?: number | null;
  parentOrderScope?: number | null;
  orderIndex?: number | null;
  labelOverride?: string | null;
  valueJson?: string | null;
  selectedOptionId?: number | null;
  selectedOptionLabel?: string | null;
  activationState?: ActivationState;
  lockedByGolden: boolean;
  userModified?: boolean;
  computed?: boolean;
}