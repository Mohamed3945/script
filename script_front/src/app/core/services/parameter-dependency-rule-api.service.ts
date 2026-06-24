import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ParameterDependencyRule } from '../models/parameter-dependency-rule.model';

@Injectable({ providedIn: 'root' })
export class ParameterDependencyRuleApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getRules(): Observable<ParameterDependencyRule[]> {
    return this.http.get<ParameterDependencyRule[]>(`${this.baseUrl}/parameter-dependency-rules`);
  }

  getRule(id: number): Observable<ParameterDependencyRule> {
    return this.http.get<ParameterDependencyRule>(`${this.baseUrl}/parameter-dependency-rules/${id}`);
  }

  createRule(payload: ParameterDependencyRule): Observable<ParameterDependencyRule> {
    return this.http.post<ParameterDependencyRule>(`${this.baseUrl}/parameter-dependency-rules`, payload);
  }

  updateRule(id: number, payload: ParameterDependencyRule): Observable<ParameterDependencyRule> {
    return this.http.put<ParameterDependencyRule>(`${this.baseUrl}/parameter-dependency-rules/${id}`, payload);
  }

  deleteRule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/parameter-dependency-rules/${id}`);
  }
}