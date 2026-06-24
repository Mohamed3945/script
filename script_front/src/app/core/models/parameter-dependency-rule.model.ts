import { RuleEffect } from './rule-effect.model';
import { RuleScope } from './rule-scope.model';

export interface ParameterDependencyRule {
  id?: number;
  sourceDefinitionId: number;
  triggerOptionId: number;
  targetDefinitionId: number;
  effect: RuleEffect;
  scope: RuleScope;
  priority: number;
}