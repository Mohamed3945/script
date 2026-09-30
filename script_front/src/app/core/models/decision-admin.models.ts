export interface DecisionQuestionAdminRequest {
  code: string;
  label: string;
  questionType: string;
  entryPoint: boolean;
  orderIndex: number;
  active: boolean;
}

export interface DecisionOptionAdminRequest {
  label: string;
  value: string;
  orderIndex: number;
}

export interface DecisionTransitionAdmin {
  id: number;
  currentQuestionId: number;
  currentQuestionCode: string;
  currentQuestionLabel: string;
  optionId: number;
  optionLabel: string;
  nextQuestionId: number | null;
  nextQuestionCode: string | null;
  resultProfileId: number | null;
  resultProfileCode: string | null;
}

export interface DecisionTransitionAdminRequest {
  currentQuestionId: number | null;
  optionId: number | null;
  nextQuestionId: number | null;
  resultProfileId: number | null;
}

export interface DecisionResultProfile {
  id: number;
  code: string;
  description: string | null;
  active: boolean;
  goldenRecipeId: number | null;
}

export interface DecisionResultProfileAdminRequest {
  code: string;
  description: string | null;
  active: boolean;
  goldenRecipeId: number | null;
}
