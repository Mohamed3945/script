export type EndpointOperator = 'EQ' | 'NEQ' | 'LT' | 'LTE' | 'GT' | 'GTE';

export const OPERATOR_LABELS: Record<EndpointOperator, string> = {
  EQ: '=',
  NEQ: '≠',
  LT: '<',
  LTE: '≤',
  GT: '>',
  GTE: '≥'
};