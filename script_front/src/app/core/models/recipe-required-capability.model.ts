import { CapabilityCategory } from './capability-category.model';

export interface RecipeRequiredCapability {
  capabilityId: number;
  capabilityCode: string;
  capabilityLabel: string;
  capabilityCategory: CapabilityCategory;
}
