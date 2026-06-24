import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, map } from 'rxjs';
import { ParameterDefinition } from '../models/parameter-definition.model';
import { ParameterDefinitionDetail } from '../models/parameter-definition-detail.model';
import { ParameterOption } from '../models/parameter-option.model';

@Injectable({ providedIn: 'root' })
export class ParameterDefinitionApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getDefinitions(): Observable<ParameterDefinition[]> {
    return this.http.get<ParameterDefinition[]>(`${this.baseUrl}/parameter-definitions`);
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
}