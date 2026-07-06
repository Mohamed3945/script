import { CapabilityCategory } from './capability-category.model';

export interface ChamberCapability {
  id?: number;
  code: string;
  label: string;
  category: CapabilityCategory;
  active: boolean;
}
