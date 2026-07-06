import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RecipeRequirements } from '../models/recipe-requirements.model';

@Injectable({ providedIn: 'root' })
export class RecipeRequirementsApiService {
  private readonly baseUrl = 'http://localhost:8080/api/recipes';

  constructor(private http: HttpClient) {}

  getRequirements(recipeId: number): Observable<RecipeRequirements> {
    return this.http.get<RecipeRequirements>(`${this.baseUrl}/${recipeId}/requirements`);
  }

  addRequiredCapability(recipeId: number, capabilityId: number): Observable<RecipeRequirements> {
    return this.http.post<RecipeRequirements>(
      `${this.baseUrl}/${recipeId}/required-capabilities/${capabilityId}`,
      {}
    );
  }

  removeRequiredCapability(recipeId: number, capabilityId: number): Observable<RecipeRequirements> {
    return this.http.delete<RecipeRequirements>(
      `${this.baseUrl}/${recipeId}/required-capabilities/${capabilityId}`
    );
  }

  addRequiredConfiguration(
    recipeId: number,
    configurationDefinitionId: number
  ): Observable<RecipeRequirements> {
    return this.http.post<RecipeRequirements>(
      `${this.baseUrl}/${recipeId}/required-configurations/${configurationDefinitionId}`,
      {}
    );
  }

  removeRequiredConfiguration(
    recipeId: number,
    configurationDefinitionId: number
  ): Observable<RecipeRequirements> {
    return this.http.delete<RecipeRequirements>(
      `${this.baseUrl}/${recipeId}/required-configurations/${configurationDefinitionId}`
    );
  }
}
