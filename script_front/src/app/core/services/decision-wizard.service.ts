import { Injectable } from '@angular/core';
import { BehaviorSubject, finalize } from 'rxjs';
import { DecisionApiService } from './decision-api.service';
import { DecisionQuestion } from '../models/decision-question.model';
import { DecisionResultProfile } from '../models/decision-result-profile.model';
import { WizardSelection } from '../models/wizard-selection.model';

@Injectable({
  providedIn: 'root'
})
export class DecisionWizardService {
  private currentQuestionSubject = new BehaviorSubject<DecisionQuestion | null>(null);
  private selectedOptionIdSubject = new BehaviorSubject<number | null>(null);
  private selectionsSubject = new BehaviorSubject<WizardSelection[]>([]);
  private resultSubject = new BehaviorSubject<DecisionResultProfile | null>(null);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  currentQuestion$ = this.currentQuestionSubject.asObservable();
  selectedOptionId$ = this.selectedOptionIdSubject.asObservable();
  selections$ = this.selectionsSubject.asObservable();
  result$ = this.resultSubject.asObservable();
  loading$ = this.loadingSubject.asObservable();

  private questionHistory: DecisionQuestion[] = [];

  constructor(private api: DecisionApiService) {}

  reset(): void {
    this.currentQuestionSubject.next(null);
    this.selectedOptionIdSubject.next(null);
    this.selectionsSubject.next([]);
    this.resultSubject.next(null);
    this.loadingSubject.next(false);
    this.questionHistory = [];
  }

  loadEntryPoint(onResult: () => void = () => {}): void {
    this.loadingSubject.next(true);
    this.api.getEntryPointQuestion()
      .pipe(finalize(() => this.loadingSubject.next(false)))
      .subscribe({
        next: (question) => {
          this.currentQuestionSubject.next(question);
          this.selectedOptionIdSubject.next(null);
          onResult();
        },
        error: (err) => {
          console.error('Failed to load entry point question', err);
        }
      });
  }

  selectOption(optionId: number): void {
    this.selectedOptionIdSubject.next(optionId);
  }

  goNext(onQuestion: () => void = () => {}, onResult: () => void = () => {}): void {
    const currentQuestion = this.currentQuestionSubject.value;
    const selectedOptionId = this.selectedOptionIdSubject.value;

    if (!currentQuestion || !selectedOptionId) {
      return;
    }

    const selectedOption = currentQuestion.options.find(o => o.id === selectedOptionId);
    if (!selectedOption) {
      return;
    }

    const newSelection: WizardSelection = {
      questionLabel: currentQuestion.label,
      optionLabel: selectedOption.label,
      questionId: currentQuestion.id,
      optionId: selectedOption.id
    };

    this.questionHistory.push(currentQuestion);
    this.selectionsSubject.next([...this.selectionsSubject.value, newSelection]);

    this.loadingSubject.next(true);
    this.api.getNextTransition({
      currentQuestionId: currentQuestion.id,
      selectedOptionId
    })
    .pipe(finalize(() => this.loadingSubject.next(false)))
    .subscribe({
      next: (response) => {
        if (response.type === 'QUESTION' && response.nextQuestion) {
          this.currentQuestionSubject.next(response.nextQuestion);
          this.selectedOptionIdSubject.next(null);
          onQuestion();
        } else if (response.type === 'RESULT' && response.resultProfile) {
          this.resultSubject.next(response.resultProfile);
          onResult();
        }
      },
      error: (err) => {
        console.error('Failed to resolve next transition', err);
      }
    });
  }

  goBack(): void {
    const selections = [...this.selectionsSubject.value];
    if (selections.length === 0) {
      return;
    }

    selections.pop();
    this.selectionsSubject.next(selections);

    const previousQuestion = this.questionHistory.pop() || null;
    this.currentQuestionSubject.next(previousQuestion);
    this.selectedOptionIdSubject.next(null);
  }
}