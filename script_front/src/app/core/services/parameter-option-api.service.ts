import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ParameterOption } from '../models/parameter-option.model';

@Injectable({ providedIn: 'root' })
/**
 * ParameterOptionApiService coordinates UI logic for this feature.
 */
export class ParameterOptionApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /**
   * Handles the getOptionsByDefinition workflow.
   */
  getOptionsByDefinition(definitionId: number): Observable<ParameterOption[]> {
    return this.http.get<ParameterOption[]>(`${this.baseUrl}/parameter-definitions/${definitionId}/options`);
  }

  /**
   * Handles the createOption workflow.
   */
  createOption(payload: ParameterOption): Observable<ParameterOption> {
    return this.http.post<ParameterOption>(`${this.baseUrl}/parameter-options`, payload);
  }

  /**
   * Handles the updateOption workflow.
   */
  updateOption(id: number, payload: ParameterOption): Observable<ParameterOption> {
    return this.http.put<ParameterOption>(`${this.baseUrl}/parameter-options/${id}`, payload);
  }

  /**
   * Handles the deleteOption workflow.
   */
  deleteOption(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/parameter-options/${id}`);
  }
}
