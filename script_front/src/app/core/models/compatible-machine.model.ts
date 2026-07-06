import { PlatformType } from './platform-type.model';
import { CompatibleChamber } from './compatible-chamber.model';

export interface CompatibleMachine {
  machineId: number;
  machineCode: string;
  machineName: string;
  platformType: PlatformType;
  compatibleChambers: CompatibleChamber[];
}
