import { ChamberCapability } from './chamber-capability.model';
import { ChamberConfiguration } from './chamber-configuration.model';

export interface ChamberDetail {
  id: number;
  machineId: number;
  machineCode: string;
  machineName: string;
  code: string;
  name: string;
  capabilities: ChamberCapability[];
  configurations: ChamberConfiguration[];
}
