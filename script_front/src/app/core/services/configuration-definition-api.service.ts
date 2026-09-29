import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ConfigurationDefinition } from '../models/configuration-definition.model';
import { ConfigurationDefinitionDetail } from '../models/configuration-definition-detail.model';

@Injectable({ providedIn: 'root' })
export class ConfigurationDefinitionApiService {
  private readonly baseUrl = `${environment.apiBaseUrl}/configuration-definitions`;

  constructor(private http: HttpClient) {}

  getDefinitions(): Observable<ConfigurationDefinition[]> {
    return this.http.get<ConfigurationDefinition[]>(this.baseUrl);
  }

  getDefinition(id: number): Observable<ConfigurationDefinitionDetail> {
    return this.http.get<ConfigurationDefinitionDetail>(`${this.baseUrl}/${id}`);
  }

  createDefinition(payload: ConfigurationDefinition): Observable<ConfigurationDefinition> {
    return this.http.post<ConfigurationDefinition>(this.baseUrl, payload);
  }

  updateDefinition(id: number, payload: ConfigurationDefinition): Observable<ConfigurationDefinition> {
    return this.http.put<ConfigurationDefinition>(`${this.baseUrl}/${id}`, payload);
  }

  deleteDefinition(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
