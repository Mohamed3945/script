import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ParameterDependencyRule } from '../models/parameter-dependency-rule.model';
import { ParameterDependencyRuleView } from '../models/parameter-dependency-rule-view.model';

@Injectable({ providedIn: 'root' })
/**
 * ParameterDependencyRuleApiService coordinates UI logic for this feature.
 */
export class ParameterDependencyRuleApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /**
   * Handles the getRules workflow.
   */
  getRules(): Observable<ParameterDependencyRuleView[]> {
    return this.http.get<ParameterDependencyRuleView[]>(`${this.baseUrl}/parameter-dependency-rules`);
  }

  /**
   * Handles the getRule workflow.
   */
  getRule(id: number): Observable<ParameterDependencyRuleView> {
    return this.http.get<ParameterDependencyRuleView>(`${this.baseUrl}/parameter-dependency-rules/${id}`);
  }

  /**
   * Handles the createRule workflow.
   */
  createRule(payload: ParameterDependencyRule): Observable<ParameterDependencyRule> {
    return this.http.post<ParameterDependencyRule>(`${this.baseUrl}/parameter-dependency-rules`, payload);
  }

  /**
   * Handles the updateRule workflow.
   */
  updateRule(id: number, payload: ParameterDependencyRule): Observable<ParameterDependencyRule> {
    return this.http.put<ParameterDependencyRule>(`${this.baseUrl}/parameter-dependency-rules/${id}`, payload);
  }

  /**
   * Handles the deleteRule workflow.
   */
  deleteRule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/parameter-dependency-rules/${id}`);
  }
}
