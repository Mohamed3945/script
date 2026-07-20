import { StepType } from './step-type.model';

export interface ParameterGroup {
  id?: number;
  name: string;
  stepType: StepType;
  orderIndex?: number | null;
}