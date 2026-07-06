import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RecipeCompatibleMachineSearchRequest } from '../models/recipe-compatible-machine-search-request.model';
import { RecipeCompatibilityResult } from '../models/recipe-compatibility-result.model';

@Injectable({ providedIn: 'root' })
export class RecipeCompatibilityApiService {
  private readonly baseUrl = 'http://localhost:8080/api/recipe-compatibility';

  constructor(private http: HttpClient) {}

  findCompatibleMachines(
    request: RecipeCompatibleMachineSearchRequest
  ): Observable<RecipeCompatibilityResult> {
    return this.http.post<RecipeCompatibilityResult>(
      `${this.baseUrl}/compatible-machines`,
      request
    );
  }
}
