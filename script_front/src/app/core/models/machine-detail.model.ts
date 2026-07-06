import { PlatformType } from './platform-type.model';
import { Chamber } from './chamber.model';

export interface MachineDetail {
  id: number;
  code: string;
  name: string;
  platformType: PlatformType;
  chambers: Chamber[];
}
