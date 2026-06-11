import { DecisionOption } from './decision-option.model';

export interface DecisionQuestion {
  id: number;
  code: string;
  label: string;
  orderIndex: number;
  entryPoint: boolean;
  active: boolean;
  questionType: string;
  options: DecisionOption[];
}
