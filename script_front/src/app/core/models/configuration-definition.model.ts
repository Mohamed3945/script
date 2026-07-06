import { ConfigurationValueType } from './configuration-value-type.model';

export interface ConfigurationDefinition {
  id?: number;
  code: string;
  name: string;
  valueType: ConfigurationValueType;
  unit?: string | null;
  questionForForm: string;
  questionGroup?: string | null;
  displayOrder: number;
  active: boolean;
}
