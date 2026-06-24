import { ParameterValueType } from './parameter-value-type.model';

export interface ParameterDefinition {
  id?: number;
  code?: string;
  name: string;
  alias: string;
  unit?: string | null;
  description?: string | null;
  valueType: ParameterValueType;
  requiredOnStep: boolean;
  defaultValueJson?: string | null;
}