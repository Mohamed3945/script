import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, forkJoin, map } from 'rxjs';
import { ParameterDefinition } from '../models/parameter-definition.model';
import { ParameterDefinitionDetail } from '../models/parameter-definition-detail.model';
import { ParameterOption } from '../models/parameter-option.model';
import { StepType } from '../models/step-type.model';

@Injectable({ providedIn: 'root' })
/**
 * ParameterDefinitionApiService coordinates UI logic for this feature.
 */
export class ParameterDefinitionApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /**
   * Handles the getDefinitions workflow.
   */
  getDefinitions(stepType?: StepType): Observable<ParameterDefinition[]> {
    let params = new HttpParams();
    if (stepType) {
      params = params.set('stepType', stepType);
    }
    return this.http.get<ParameterDefinition[]>(`${this.baseUrl}/parameter-definitions`, { params });
  }

  /**
   * Handles the getDefinition workflow.
   */
  getDefinition(id: number): Observable<ParameterDefinition> {
    return this.http.get<ParameterDefinition>(`${this.baseUrl}/parameter-definitions/${id}`);
  }

  /**
   * Handles the getDefinitionOptions workflow.
   */
  getDefinitionOptions(id: number): Observable<ParameterOption[]> {
    return this.http.get<ParameterOption[]>(`${this.baseUrl}/parameter-definitions/${id}/options`);
  }

  /**
   * Handles the getDefinitionDetail workflow.
   */
  getDefinitionDetail(id: number): Observable<ParameterDefinitionDetail> {
    return forkJoin({
      definition: this.getDefinition(id),
      options: this.getDefinitionOptions(id)
    }).pipe(
      map(({ definition, options }) => ({
        ...definition,
        options
      }))
    );
  }

  /**
   * Handles the createDefinition workflow.
   */
  createDefinition(payload: ParameterDefinition): Observable<ParameterDefinition> {
    return this.http.post<ParameterDefinition>(`${this.baseUrl}/parameter-definitions`, payload);
  }

  /**
   * Handles the updateDefinition workflow.
   */
  updateDefinition(id: number, payload: ParameterDefinition): Observable<ParameterDefinition> {
    return this.http.put<ParameterDefinition>(`${this.baseUrl}/parameter-definitions/${id}`, payload);
  }

  /**
   * Handles the deleteDefinition workflow.
   */
  deleteDefinition(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/parameter-definitions/${id}`);
  }
}
