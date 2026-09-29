import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Recipe } from '../models/recipe.model';
import { Step } from '../models/step.model';
import { StepParameter } from '../models/step-parameter.model';
import { StepParameterGridRow } from '../models/step-parameter-grid-row.model';
import { RecipeMatrix } from '../models/recipe-matrix.model';
import { RecipeKind } from '../models/recipe-kind.model';
import { StepKind } from '../models/step-kind.model';
import { StepEndpoint } from '../models/step-endpoint.model';
import { RecipeCustomizationSummary } from '../models/recipe-customization-summary.model';

export interface DuplicateGoldenRecipePayload {
  sourceGoldenRecipeId: number;
  targetName: string;
  creatorId?: number;
  includeFormulas: boolean;
  includeRequiredCapabilities: boolean;
  includeRequiredConfigurations: boolean;
}

@Injectable({ providedIn: 'root' })
export class RecipeApiService {
  private readonly baseUrl = environment.apiBaseUrl;

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

  duplicateGoldenRecipe(payload: DuplicateGoldenRecipePayload): Observable<Recipe> {
    return this.http.post<Recipe>(`${this.baseUrl}/recipes/duplicate-golden`, payload);
  }

  updateRecipe(id: number, payload: Recipe): Observable<Recipe> {
    return this.http.put<Recipe>(`${this.baseUrl}/recipes/${id}`, payload);
  }

  deleteRecipe(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/recipes/${id}`);
  }

  deleteRecipeWideParameter(recipeId: number, definitionId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/recipes/${recipeId}/parameter-definitions/${definitionId}`);
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

  createStructuredStep(
    recipeId: number,
    payload: {
      name?: string | null;
      stepKind?: StepKind | null;
      orderIndex?: number | null;
    }
  ): Observable<Step> {
    return this.http.post<Step>(`${this.baseUrl}/recipes/${recipeId}/steps/structured`, payload);
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

  getRecipeMatrix(recipeId: number): Observable<RecipeMatrix> {
    return this.http.get<RecipeMatrix>(`${this.baseUrl}/recipes/${recipeId}/matrix`);
  }

  createStepParametersBulk(stepId: number, payload: StepParameter[]): Observable<StepParameter[]> {
    return this.http.post<StepParameter[]>(`${this.baseUrl}/steps/${stepId}/parameters/bulk`, payload);
  }

  propagateStepParameter(
    recipeId: number,
    stepKind: StepKind,
    payload: StepParameter
  ): Observable<StepParameter[]> {
    const params = new HttpParams().set('stepKind', stepKind);
    return this.http.post<StepParameter[]>(
      `${this.baseUrl}/recipes/${recipeId}/step-parameters/propagate`,
      payload,
      { params }
    );
  }

  getStepEndpoint(stepId: number): Observable<StepEndpoint> {
    return this.http.get<StepEndpoint>(`${this.baseUrl}/steps/${stepId}/endpoint`);
  }

  upsertStepEndpoint(stepId: number, payload: StepEndpoint): Observable<StepEndpoint> {
    return this.http.put<StepEndpoint>(`${this.baseUrl}/steps/${stepId}/endpoint`, payload);
  }

  deleteStepEndpoint(stepId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/steps/${stepId}/endpoint`);
  }

  exportRecipeXml(recipeId: number, machineId?: number | null): Observable<HttpResponse<Blob>> {
    let params = new HttpParams();
    if (machineId != null) {
      params = params.set('machineId', String(machineId));
    }
    return this.http.get(`${this.baseUrl}/recipes/${recipeId}/export/xml`, {
      params,
      observe: 'response',
      responseType: 'blob'
    });
  }

  getCustomizationSummary(recipeId: number, limit = 100): Observable<RecipeCustomizationSummary> {
    const params = new HttpParams().set('limit', String(limit));
    return this.http.get<RecipeCustomizationSummary>(`${this.baseUrl}/recipes/${recipeId}/customization-summary`, {
      params
    });
  }
}