import { ActivationState } from './activation-state.model';
import { ParameterOption } from './parameter-option.model';
import { ParameterValueType } from './parameter-value-type.model';

export interface RecipeMatrixCell {
  stepId: number;
  stepParameterId?: number | null;
  definitionId: number;
  valueType: ParameterValueType;

  displayValue: string;
  valueJson?: string | null;
  selectedOptionId?: number | null;
  selectedOptionLabel?: string | null;

  availableOptions: ParameterOption[];

  activationState?: ActivationState | null;
  lockedByGolden: boolean;
  editable: boolean;
  userModified: boolean;
  computedFromModified: boolean;
  computed: boolean;
}
