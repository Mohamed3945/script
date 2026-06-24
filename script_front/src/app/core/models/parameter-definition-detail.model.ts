import { ParameterValueType } from './parameter-value-type.model';
import { ParameterOption } from './parameter-option.model';

export interface ParameterDefinitionDetail {
  id?: number;
  code?: string;
  name: string;
  alias: string;
  unit?: string | null;
  description?: string | null;
  valueType: ParameterValueType;
  requiredOnStep: boolean;
  defaultValueJson?: string | null;
  options: ParameterOption[];
}