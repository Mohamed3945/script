import { ConfigurationValueType } from './configuration-value-type.model';

export interface RecipeRequiredConfiguration {
  configurationDefinitionId: number;
  configurationDefinitionCode?: string | null;
  configurationDefinitionName?: string | null;
  valueType: ConfigurationValueType;
  unit?: string | null;
  questionForForm: string;
  questionGroup?: string | null;
  displayOrder?: number | null;
}
