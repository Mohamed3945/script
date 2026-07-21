import { EndpointOperator } from './endpoint-operator.model';
import { ParameterValueType } from './parameter-value-type.model';

export interface StepEndpointCondition {
  id: number | null;
  endpointParameterId: number;
  endpointParameterAlias?: string | null;
  endpointParameterName?: string | null;
  endpointParameterUnit?: string | null;
  endpointParameterValueType?: ParameterValueType | null;
  valueJson: string | null;
  selectedOptionId: number | null;
  selectedOptionLabel?: string | null;
  operator: EndpointOperator;
  orderIndex: number;
}

export interface StepEndpoint {
  id: number | null;
  stepId: number;
  clause: 'AND' | 'OR' | null;
  lockedByGolden?: boolean | null;
  conditions: StepEndpointCondition[];
}