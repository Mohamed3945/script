import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, forkJoin, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ParameterDefinition } from '../models/parameter-definition.model';
import { ParameterDefinitionDetail } from '../models/parameter-definition-detail.model';
import { ParameterOption } from '../models/parameter-option.model';
import { ParameterScope } from '../models/parameter-scope.model';

@Injectable({ providedIn: 'root' })
export class ParameterDefinitionApiService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getDefinitions(stepType?: ParameterScope): Observable<ParameterDefinition[]> {
    let params = new HttpParams();
    if (stepType) {
      params = params.set('stepType', stepType);
    }
    return this.http.get<ParameterDefinition[]>(`${this.baseUrl}/parameter-definitions`, { params });
  }

  getDefinition(id: number): Observable<ParameterDefinition> {
    return this.http.get<ParameterDefinition>(`${this.baseUrl}/parameter-definitions/${id}`);
  }

  getDefinitionOptions(id: number): Observable<ParameterOption[]> {
    return this.http.get<ParameterOption[]>(`${this.baseUrl}/parameter-definitions/${id}/options`);
  }

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

  createDefinition(payload: ParameterDefinition): Observable<ParameterDefinition> {
    return this.http.post<ParameterDefinition>(`${this.baseUrl}/parameter-definitions`, payload);
  }

  updateDefinition(id: number, payload: ParameterDefinition): Observable<ParameterDefinition> {
    return this.http.put<ParameterDefinition>(`${this.baseUrl}/parameter-definitions/${id}`, payload);
  }

  deleteDefinition(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/parameter-definitions/${id}`);
  }

  reorderDefinitions(parameterGroupId: number, orderedDefinitionIds: number[]): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/parameter-definitions/reorder`, {
      parameterGroupId,
      orderedDefinitionIds
    });
  }
  getByConfigurationDefinition(configDefId: number): Observable<ParameterDefinition[]> {
    return this.http.get<ParameterDefinition[]>(`${this.baseUrl}/parameter-definitions/by-configuration-definition/${configDefId}`);
  }

  moveDefinition(definitionId: number, targetGroupId: number, targetIndex: number): Observable<ParameterDefinition> {
    return this.http.post<ParameterDefinition>(`${this.baseUrl}/parameter-definitions/move`, {
      definitionId,
      targetGroupId,
      targetIndex
    });
  }
}