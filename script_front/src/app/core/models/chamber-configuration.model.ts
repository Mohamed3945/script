export interface ChamberConfiguration {
  id?: number;

  chamberId?: number;
  chamberCode?: string;
  chamberName?: string;

  configurationDefinitionId: number;
  configurationDefinitionCode?: string;
  configurationDefinitionName?: string;

  chamberConfigurationCode: string;
  chamberConfigurationName: string;

  nominalValue?: number | null;
  minValue?: number | null;
  maxValue?: number | null;
}
