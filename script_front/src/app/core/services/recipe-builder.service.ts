import { Injectable } from '@angular/core';
import { BehaviorSubject, forkJoin } from 'rxjs';
import { Recipe } from '../models/recipe.model';
import { Step } from '../models/step.model';
import { StepParameter } from '../models/step-parameter.model';
import { RecipeMatrix } from '../models/recipe-matrix.model';
import { RecipeApiService } from './recipe-api.service';

@Injectable({
  providedIn: 'root'
})
export class RecipeBuilderService {
  private recipeSubject = new BehaviorSubject<Recipe | null>(null);
  private stepsSubject = new BehaviorSubject<Step[]>([]);
  private selectedStepSubject = new BehaviorSubject<Step | null>(null);
  private stepParametersSubject = new BehaviorSubject<StepParameter[]>([]);
  private recipeMatrixSubject = new BehaviorSubject<RecipeMatrix | null>(null);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  recipe$ = this.recipeSubject.asObservable();
  steps$ = this.stepsSubject.asObservable();
  selectedStep$ = this.selectedStepSubject.asObservable();
  stepParameters$ = this.stepParametersSubject.asObservable();
  recipeMatrix$ = this.recipeMatrixSubject.asObservable();
  loading$ = this.loadingSubject.asObservable();

  get selectedStepSnapshot(): Step | null {
    return this.selectedStepSubject.value;
  }

  constructor(private recipeApiService: RecipeApiService) {}

  setRecipe(recipe: Recipe): void {
    this.recipeSubject.next(recipe);
  }

  setSelectedStep(step: Step | null): void {
    this.selectedStepSubject.next(step);
  }

  loadRecipeWorkspace(recipe: Recipe): void {
    if (!recipe.id) {
      return;
    }

    this.recipeSubject.next(recipe);
    this.loadingSubject.next(true);

    forkJoin({
      steps: this.recipeApiService.getRecipeSteps(recipe.id),
      matrix: this.recipeApiService.getRecipeMatrix(recipe.id)
    }).subscribe({
      next: ({ steps, matrix }) => {
        this.stepsSubject.next(steps);
        this.recipeMatrixSubject.next(matrix);

        const selectedStep = steps.find((step) => step.stepKind !== 'PRESTEP') ?? null;
        this.selectedStepSubject.next(selectedStep);

        if (selectedStep?.id) {
          this.recipeApiService.getStepParameters(selectedStep.id).subscribe({
            next: (parameters) => {
              this.stepParametersSubject.next(parameters);
              this.loadingSubject.next(false);
            },
            error: (error) => {
              console.error('Failed to load step parameters', error);
              this.stepParametersSubject.next([]);
              this.loadingSubject.next(false);
            }
          });
        } else {
          this.stepParametersSubject.next([]);
          this.loadingSubject.next(false);
        }
      },
      error: (error) => {
        console.error('Failed to load recipe workspace', error);
        this.stepsSubject.next([]);
        this.recipeMatrixSubject.next(null);
        this.stepParametersSubject.next([]);
        this.selectedStepSubject.next(null);
        this.loadingSubject.next(false);
      }
    });
  }

  loadStepParameters(stepId: number): void {
    this.loadingSubject.next(true);
    this.recipeApiService.getStepParameters(stepId).subscribe({
      next: (parameters) => {
        this.stepParametersSubject.next(parameters);
        this.loadingSubject.next(false);
      },
      error: (error) => {
        console.error('Failed to load step parameters', error);
        this.stepParametersSubject.next([]);
        this.loadingSubject.next(false);
      }
    });
  }

  refreshMatrix(): void {
    const recipe = this.recipeSubject.value;
    if (!recipe?.id) {
      return;
    }

    this.recipeApiService.getRecipeMatrix(recipe.id).subscribe({
      next: (matrix) => this.recipeMatrixSubject.next(matrix),
      error: (error) => console.error('Failed to refresh recipe matrix', error)
    });
  }

  reset(): void {
    this.recipeSubject.next(null);
    this.stepsSubject.next([]);
    this.selectedStepSubject.next(null);
    this.stepParametersSubject.next([]);
    this.recipeMatrixSubject.next(null);
    this.loadingSubject.next(false);
  }
}