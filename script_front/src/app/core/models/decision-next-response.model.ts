import { DecisionQuestion } from './decision-question.model';
import { DecisionResultProfile } from './decision-result-profile.model';
import { NextTransitionType } from './next-transition-type.model';

export interface DecisionNextResponse {
  type: NextTransitionType;
  nextQuestion: DecisionQuestion | null;
  resultProfile: DecisionResultProfile | null;
}
