import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Recipe } from '../models/recipe.model';
import { Step } from '../models/step.model';
import { StepParameter } from '../models/step-parameter.model';
import { StepParameterGridRow } from '../models/step-parameter-grid-row.model';
import { RecipeKind } from '../models/recipe-kind.model';
import { StepKind } from '../models/step-kind.model';

@Injectable({ providedIn: 'root' })
export class RecipeApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getRecipes(filters?: { recipeKind?: RecipeKind; golden?: boolean }): Observable<Recipe[]> {
    let params = new HttpParams();
    if (filters?.recipeKind) params = params.set('recipeKind', filters.recipeKind);
    if (filters?.golden !== undefined) params = params.set('golden', String(filters.golden));
    return this.http.get<Recipe[]>(`${this.baseUrl}/recipes`, { params });
  }

  getRecipeById(id: number): Observable<Recipe> {
    return this.http.get<Recipe>(`${this.baseUrl}/recipes/${id}`);
  }

  createRecipe(payload: Recipe, resultProfileId?: number): Observable<Recipe> {
    let params = new HttpParams();
    if (resultProfileId != null) {
      params = params.set('resultProfileId', resultProfileId);
    }
    return this.http.post<Recipe>(`${this.baseUrl}/recipes`, payload, { params });
  }

  updateRecipe(id: number, payload: Recipe): Observable<Recipe> {
    return this.http.put<Recipe>(`${this.baseUrl}/recipes/${id}`, payload);
  }

  deleteRecipe(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/recipes/${id}`);
  }

  getRecipeSteps(recipeId: number, stepKind?: StepKind): Observable<Step[]> {
    let params = new HttpParams();
    if (stepKind) params = params.set('stepKind', stepKind);
    return this.http.get<Step[]>(`${this.baseUrl}/recipes/${recipeId}/steps`, { params });
  }

  getStepById(id: number): Observable<Step> {
    return this.http.get<Step>(`${this.baseUrl}/steps/${id}`);
  }

  createStep(recipeId: number, payload: Step): Observable<Step> {
    return this.http.post<Step>(`${this.baseUrl}/recipes/${recipeId}/steps`, payload);
  }

  updateStep(id: number, payload: Step): Observable<Step> {
    return this.http.put<Step>(`${this.baseUrl}/steps/${id}`, payload);
  }

  deleteStep(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/steps/${id}`);
  }

  getStepParameters(stepId: number): Observable<StepParameter[]> {
    return this.http.get<StepParameter[]>(`${this.baseUrl}/steps/${stepId}/parameters`);
  }

  getStepParameterById(id: number): Observable<StepParameter> {
    return this.http.get<StepParameter>(`${this.baseUrl}/step-parameters/${id}`);
  }

  createStepParameter(stepId: number, payload: StepParameter): Observable<StepParameter> {
    return this.http.post<StepParameter>(`${this.baseUrl}/steps/${stepId}/parameters`, payload);
  }

  updateStepParameter(id: number, payload: StepParameter): Observable<StepParameter> {
    return this.http.put<StepParameter>(`${this.baseUrl}/step-parameters/${id}`, payload);
  }

  deleteStepParameter(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/step-parameters/${id}`);
  }

  getRecipeStepParameterGrid(recipeId: number): Observable<StepParameterGridRow[]> {
    return this.http.get<StepParameterGridRow[]>(`${this.baseUrl}/recipes/${recipeId}/step-parameter-grid`);
  }
}