import { PlatformType } from './platform-type.model';

export interface Machine {
  id?: number;
  code: string;
  name: string;
  platformType: PlatformType;
}
