import { ActivationState } from './activation-state.model';
import { ParameterValueType } from './parameter-value-type.model';
import { StepKind } from './step-kind.model';

export interface StepParameterGridRow {
  recipeId: number;
  stepId: number;
  stepName: string;
  stepKind: StepKind;
  stepOrderIndex: number;

  stepParameterId: number;
  parentStepParameterId?: number | null;
  parentOrderScope: number;
  parameterOrderIndex: number;

  parameterDefinitionId: number;
  parameterDefinitionName: string;
  parameterValueType: ParameterValueType;

  labelOverride?: string | null;
  valueJson?: string | null;
  selectedOptionId?: number | null;
  selectedOptionLabel?: string | null;

  activationState: ActivationState;
  lockedByGolden: boolean;
}