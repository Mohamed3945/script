import { RuleEffect } from './rule-effect.model';
import { RuleScope } from './rule-scope.model';

export interface ParameterDependencyRuleView {
  id: number;

  sourceDefinitionId: number;
  sourceDefinitionCode: string;
  sourceDefinitionName: string;

  triggerOptionId: number;
  triggerOptionCode: string;
  triggerOptionLabel: string;

  requiredSourceActivationOptionId?: number | null;
  requiredSourceActivationOptionCode?: string | null;
  requiredSourceActivationOptionLabel?: string | null;

  targetDefinitionId: number;
  targetDefinitionCode: string;
  targetDefinitionName: string;

  effect: RuleEffect;
  scope: RuleScope;
  priority: number;
}
