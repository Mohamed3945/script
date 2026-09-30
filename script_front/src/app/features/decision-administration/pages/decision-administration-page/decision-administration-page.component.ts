import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  DecisionOptionAdminRequest,
  DecisionQuestionAdminRequest,
  DecisionResultProfile,
  DecisionResultProfileAdminRequest,
  DecisionTransitionAdmin,
  DecisionTransitionAdminRequest
} from '../../../../core/models/decision-admin.models';
import { DecisionOption } from '../../../../core/models/decision-option.model';
import { DecisionQuestion } from '../../../../core/models/decision-question.model';
import { DecisionAdminApiService } from '../../../../core/services/decision-admin-api.service';
import { Recipe } from '../../../../core/models/recipe.model';
import { RecipeApiService } from '../../../../core/services/recipe-api.service';

interface QuestionForm {
  id: number | null;
  code: string;
  label: string;
  questionType: string;
  entryPoint: boolean;
  orderIndex: number;
  active: boolean;
}

interface OptionForm {
  id: number | null;
  label: string;
  value: string;
  orderIndex: number;
}

interface TransitionForm extends DecisionTransitionAdminRequest {
  id: number | null;
}

interface ResultProfileForm {
  id: number | null;
  code: string;
  description: string;
  active: boolean;
  goldenRecipeId: number | null;
}

@Component({
  selector: 'app-decision-administration-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './decision-administration-page.component.html',
  styleUrl: './decision-administration-page.component.scss'
})
export class DecisionAdministrationPageComponent implements OnInit {
  activeTab: 'questions' | 'transitions' | 'results' = 'questions';
  questions: DecisionQuestion[] = [];
  transitions: DecisionTransitionAdmin[] = [];
  resultProfiles: DecisionResultProfile[] = [];
  goldenRecipes: Recipe[] = [];
  loading = false;
  saving = false;
  errorMessage = '';
  selectedQuestionId: number | null = null;
  targetType: 'QUESTION' | 'RESULT' = 'QUESTION';
  questionForm: QuestionForm = this.emptyQuestionForm();
  optionForm: OptionForm = this.emptyOptionForm();
  transitionForm: TransitionForm = this.emptyTransitionForm();
  resultProfileForm: ResultProfileForm = this.emptyResultProfileForm();

  readonly questionTypes = ['SINGLE_CHOICE', 'BOOLEAN', 'INFO'];

  constructor(
    private readonly api: DecisionAdminApiService,
    private readonly recipeApi: RecipeApiService
  ) {}

  ngOnInit(): void {
    this.loadQuestions();
    this.loadTransitions();
    this.api.getResultProfiles().subscribe({
      next: profiles => this.resultProfiles = profiles,
      error: () => this.errorMessage = 'Impossible de charger les profils de résultat.'
    });
    this.recipeApi.getRecipes({ recipeKind: 'GOLDEN' }).subscribe({
      next: recipes => this.goldenRecipes = recipes,
      error: () => this.errorMessage = 'Impossible de charger les recettes Golden.'
    });
  }

  get selectedQuestion(): DecisionQuestion | undefined {
    return this.questions.find(question => question.id === this.selectedQuestionId);
  }

  get transitionOptions(): DecisionOption[] {
    return this.questions.find(question => question.id === this.transitionForm.currentQuestionId)?.options ?? [];
  }

  setTab(tab: 'questions' | 'transitions' | 'results'): void {
    this.activeTab = tab;
    this.errorMessage = '';
  }

  loadQuestions(selectId?: number): void {
    this.loading = true;
    this.api.getQuestions().subscribe({
      next: questions => {
        this.questions = questions;
        this.loading = false;
        const selected = this.questions.find(question => question.id === (selectId ?? this.selectedQuestionId));
        if (selected) {
          this.editQuestion(selected);
        } else if (selectId === undefined && this.selectedQuestionId !== null) {
          this.resetQuestionForm();
        }
      },
      error: () => {
        this.errorMessage = 'Impossible de charger les questions.';
        this.loading = false;
      }
    });
  }

  loadTransitions(): void {
    this.api.getTransitions().subscribe({
      next: transitions => this.transitions = transitions,
      error: () => this.errorMessage = 'Impossible de charger les transitions.'
    });
  }

  editQuestion(question: DecisionQuestion): void {
    this.selectedQuestionId = question.id;
    this.questionForm = {
      id: question.id,
      code: question.code,
      label: question.label,
      questionType: question.questionType,
      entryPoint: question.entryPoint,
      orderIndex: question.orderIndex,
      active: question.active
    };
    this.resetOptionForm();
    this.errorMessage = '';
  }

  resetQuestionForm(): void {
    this.selectedQuestionId = null;
    this.questionForm = this.emptyQuestionForm();
    this.resetOptionForm();
    this.errorMessage = '';
  }

  saveQuestion(): void {
    this.errorMessage = '';
    this.saving = true;
    const request: DecisionQuestionAdminRequest = {
      code: this.questionForm.code.trim(),
      label: this.questionForm.label.trim(),
      questionType: this.questionForm.questionType,
      entryPoint: this.questionForm.entryPoint,
      orderIndex: Number(this.questionForm.orderIndex),
      active: this.questionForm.active
    };
    const request$ = this.questionForm.id
      ? this.api.updateQuestion(this.questionForm.id, request)
      : this.api.createQuestion(request);
    request$.subscribe({
      next: question => {
        this.saving = false;
        this.loadQuestions(question.id);
      },
      error: () => {
        this.errorMessage = 'Enregistrement de la question impossible. Vérifiez le code et les champs requis.';
        this.saving = false;
      }
    });
  }

  deleteQuestion(question: DecisionQuestion): void {
    if (!window.confirm(`Supprimer « ${question.label} » et ses réponses/transitions ?`)) return;
    this.api.deleteQuestion(question.id).subscribe({
      next: () => {
        this.resetQuestionForm();
        this.loadQuestions();
        this.loadTransitions();
      },
      error: () => this.errorMessage = 'Suppression impossible : cette question est peut-être utilisée par des exécutions enregistrées. Vous pouvez la désactiver.'
    });
  }

  editOption(option: DecisionOption): void {
    this.optionForm = { id: option.id, label: option.label, value: option.value, orderIndex: option.orderIndex };
    this.errorMessage = '';
  }

  resetOptionForm(): void {
    this.optionForm = this.emptyOptionForm();
  }

  saveOption(): void {
    if (!this.selectedQuestionId) return;
    const request: DecisionOptionAdminRequest = {
      label: this.optionForm.label.trim(),
      value: this.optionForm.value.trim(),
      orderIndex: Number(this.optionForm.orderIndex)
    };
    const request$ = this.optionForm.id
      ? this.api.updateOption(this.optionForm.id, request)
      : this.api.createOption(this.selectedQuestionId, request);
    request$.subscribe({
      next: () => {
        this.loadQuestions(this.selectedQuestionId ?? undefined);
        this.loadTransitions();
      },
      error: () => this.errorMessage = 'Enregistrement de la réponse impossible. Vérifiez les champs requis.'
    });
  }

  deleteOption(option: DecisionOption): void {
    if (!window.confirm(`Supprimer la réponse « ${option.label} » et sa transition ?`)) return;
    this.api.deleteOption(option.id).subscribe({
      next: () => {
        this.loadQuestions(this.selectedQuestionId ?? undefined);
        this.loadTransitions();
      },
      error: () => this.errorMessage = 'Suppression impossible : cette réponse est peut-être utilisée par des exécutions enregistrées.'
    });
  }

  changeTransitionQuestion(questionId: number | null): void {
    this.transitionForm.currentQuestionId = questionId;
    this.transitionForm.optionId = null;
  }

  editTransition(transition: DecisionTransitionAdmin): void {
    this.transitionForm = {
      id: transition.id,
      currentQuestionId: transition.currentQuestionId,
      optionId: transition.optionId,
      nextQuestionId: transition.nextQuestionId,
      resultProfileId: transition.resultProfileId
    };
    this.targetType = transition.nextQuestionId ? 'QUESTION' : 'RESULT';
    this.errorMessage = '';
  }

  resetTransitionForm(): void {
    this.transitionForm = this.emptyTransitionForm();
    this.targetType = 'QUESTION';
    this.errorMessage = '';
  }

  saveTransition(): void {
    this.errorMessage = '';
    const request: DecisionTransitionAdminRequest = {
      currentQuestionId: this.transitionForm.currentQuestionId,
      optionId: this.transitionForm.optionId,
      nextQuestionId: this.targetType === 'QUESTION' ? this.transitionForm.nextQuestionId : null,
      resultProfileId: this.targetType === 'RESULT' ? this.transitionForm.resultProfileId : null
    };
    this.saving = true;
    const request$ = this.transitionForm.id
      ? this.api.updateTransition(this.transitionForm.id, request)
      : this.api.createTransition(request);
    request$.subscribe({
      next: () => {
        this.saving = false;
        this.resetTransitionForm();
        this.loadTransitions();
      },
      error: () => {
        this.errorMessage = 'Enregistrement impossible. Vérifiez la question, sa réponse et la destination.';
        this.saving = false;
      }
    });
  }

  deleteTransition(transition: DecisionTransitionAdmin): void {
    if (!window.confirm('Supprimer cette transition ?')) return;
    this.api.deleteTransition(transition.id).subscribe({
      next: () => this.loadTransitions(),
      error: () => this.errorMessage = 'Suppression de la transition impossible.'
    });
  }

  editResultProfile(profile: DecisionResultProfile): void {
    this.resultProfileForm = {
      id: profile.id,
      code: profile.code,
      description: profile.description ?? '',
      active: profile.active,
      goldenRecipeId: profile.goldenRecipeId
    };
    this.errorMessage = '';
  }

  resetResultProfileForm(): void {
    this.resultProfileForm = this.emptyResultProfileForm();
    this.errorMessage = '';
  }

  saveResultProfile(): void {
    this.errorMessage = '';
    this.saving = true;
    const request: DecisionResultProfileAdminRequest = {
      code: this.resultProfileForm.code.trim(),
      description: this.resultProfileForm.description.trim() || null,
      active: this.resultProfileForm.active,
      goldenRecipeId: this.resultProfileForm.goldenRecipeId
    };
    const request$ = this.resultProfileForm.id
      ? this.api.updateResultProfile(this.resultProfileForm.id, request)
      : this.api.createResultProfile(request);
    request$.subscribe({
      next: () => {
        this.saving = false;
        this.resetResultProfileForm();
        this.loadResultProfiles();
        this.loadTransitions();
      },
      error: () => {
        this.errorMessage = 'Enregistrement du profil impossible. Vérifiez le code et la recette Golden.';
        this.saving = false;
      }
    });
  }

  deleteResultProfile(profile: DecisionResultProfile): void {
    if (!window.confirm(`Supprimer le profil « ${profile.code} » et ses transitions ?`)) return;
    this.api.deleteResultProfile(profile.id).subscribe({
      next: () => {
        this.resetResultProfileForm();
        this.loadResultProfiles();
        this.loadTransitions();
      },
      error: () => this.errorMessage = 'Suppression impossible : ce profil est peut-être utilisé par des exécutions enregistrées.'
    });
  }

  recipeName(recipeId: number | null): string {
    if (recipeId === null) return '—';
    return this.goldenRecipes.find(recipe => recipe.id === recipeId)?.name ?? `#${recipeId}`;
  }

  private loadResultProfiles(): void {
    this.api.getResultProfiles().subscribe({
      next: profiles => this.resultProfiles = profiles,
      error: () => this.errorMessage = 'Impossible de charger les profils de résultat.'
    });
  }

  private emptyQuestionForm(): QuestionForm {
    return { id: null, code: '', label: '', questionType: 'SINGLE_CHOICE', entryPoint: false, orderIndex: 0, active: true };
  }

  private emptyOptionForm(): OptionForm {
    return { id: null, label: '', value: '', orderIndex: 0 };
  }

  private emptyTransitionForm(): TransitionForm {
    return { id: null, currentQuestionId: null, optionId: null, nextQuestionId: null, resultProfileId: null };
  }

  private emptyResultProfileForm(): ResultProfileForm {
    return { id: null, code: '', description: '', active: true, goldenRecipeId: null };
  }
}