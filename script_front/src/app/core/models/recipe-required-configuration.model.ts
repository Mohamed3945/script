import { ConfigurationValueType } from './configuration-value-type.model';

export interface RecipeRequiredConfiguration {
  configurationDefinitionId: number;
  configurationDefinitionCode: string;
  configurationDefinitionName: string;
  valueType: ConfigurationValueType;
  unit?: string | null;
  questionForForm: string;
  questionGroup?: string | null;
  displayOrder?: number | null;
}
