import { ParameterValueType } from './parameter-value-type.model';
import { ParameterOption } from './parameter-option.model';
import { ParameterScope } from './parameter-scope.model';

export interface ParameterDefinitionDetail {
  id?: number;
  code?: string;
  name: string;
  alias: string;
  unit?: string | null;
  description?: string | null;
  valueType: ParameterValueType;
  requiredOnStep: boolean;
  stepType: ParameterScope;
  defaultValueJson?: string | null;

  parameterGroupId?: number | null;
  parameterGroupName?: string | null;
  parameterGroupOrder?: number | null;
  orderIndexInGroup?: number | null;

  configurationDefinitionId?: number | null;
  configurationDefinitionCode?: string | null;
  configurationDefinitionName?: string | null;

  options: ParameterOption[];
}