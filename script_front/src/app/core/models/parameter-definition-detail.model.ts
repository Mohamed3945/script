import { ParameterValueType } from './parameter-value-type.model';
import { ParameterOption } from './parameter-option.model';
import { StepType } from './step-type.model';

export interface ParameterDefinitionDetail {
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
  parameterGroup?: string | null;
  parameterGroupOrder?: number | null;
  configurationDefinitionId?: number | null;
  configurationDefinitionCode?: string | null;
  configurationDefinitionName?: string | null;
  options: ParameterOption[];
}