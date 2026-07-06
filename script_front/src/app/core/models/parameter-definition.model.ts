import { ParameterValueType } from './parameter-value-type.model';
import { StepType } from './step-type.model';

export interface ParameterDefinition {
  id?: number;
  code?: string;
  name: string;
  alias: string;
  unit?: string | null;
  description?: string | null;
  valueType: ParameterValueType;
  requiredOnStep: boolean;
  stepType: StepType;
  defaultValueJson?: string | null;
  configurationDefinitionId?: number | null;
  configurationDefinitionCode?: string | null;
  configurationDefinitionName?: string | null;
}