import { ParameterScope } from './parameter-scope.model';

export interface ParameterGroup {
  id?: number;
  name: string;
  stepType: ParameterScope;
  orderIndex?: number | null;
  systemGroup?: boolean;
}