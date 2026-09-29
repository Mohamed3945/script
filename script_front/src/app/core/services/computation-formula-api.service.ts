import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ComputationFormula } from '../models/computation-formula.model';

@Injectable({ providedIn: 'root' })
export class ComputationFormulaApiService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getFormulasByRecipe(recipeId: number): Observable<ComputationFormula[]> {
    return this.http.get<ComputationFormula[]>(`${this.baseUrl}/recipes/${recipeId}/computation-formulas`);
  }

  getFormula(id: number): Observable<ComputationFormula> {
    return this.http.get<ComputationFormula>(`${this.baseUrl}/computation-formulas/${id}`);
  }

  createFormula(recipeId: number, payload: ComputationFormula): Observable<ComputationFormula> {
    return this.http.post<ComputationFormula>(`${this.baseUrl}/recipes/${recipeId}/computation-formulas`, payload);
  }

  updateFormula(id: number, payload: ComputationFormula): Observable<ComputationFormula> {
    return this.http.put<ComputationFormula>(`${this.baseUrl}/computation-formulas/${id}`, payload);
  }

  deleteFormula(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/computation-formulas/${id}`);
  }
}
