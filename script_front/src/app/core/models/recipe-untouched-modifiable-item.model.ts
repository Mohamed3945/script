import { ParameterValueType } from './parameter-value-type.model';
import { StepKind } from './step-kind.model';

export interface RecipeUntouchedModifiableItem {
  stepId: number;
  stepCode: string | null;
  stepName: string | null;
  stepKind: StepKind | null;
  stepOrderIndex: number | null;

  definitionId: number;
  parameterName: string;
  parameterGroup: string | null;
  parameterGroupOrder: number | null;
  valueType: ParameterValueType;

  currentDisplayValue: string | null;
}
