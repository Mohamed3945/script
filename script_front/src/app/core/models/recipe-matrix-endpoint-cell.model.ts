export interface RecipeMatrixEndpointCell {
  stepId: number;
  endpointId: number | null;
  clause: 'AND' | 'OR' | null;
  conditionCount: number;
  summaryLabel: string;
  lockedByGolden: boolean;
}