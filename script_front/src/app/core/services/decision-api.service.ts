import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DecisionQuestion } from '../models/decision-question.model';
import { DecisionNextRequest } from '../models/decision-next-request.model';
import { DecisionNextResponse } from '../models/decision-next-response.model';

@Injectable({
  providedIn: 'root'
})
/**
 * DecisionApiService coordinates UI logic for this feature.
 */
export class DecisionApiService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  /**
   * Handles the getEntryPointQuestion workflow.
   */
  getEntryPointQuestion(): Observable<DecisionQuestion> {
    return this.http.get<DecisionQuestion>(`${this.baseUrl}/questions/entry-point`);
  }

  /**
   * Handles the getQuestionById workflow.
   */
  getQuestionById(id: number): Observable<DecisionQuestion> {
    return this.http.get<DecisionQuestion>(`${this.baseUrl}/questions/${id}`);
  }

  /**
   * Handles the getQuestionByCode workflow.
   */
  getQuestionByCode(code: string): Observable<DecisionQuestion> {
    return this.http.get<DecisionQuestion>(`${this.baseUrl}/questions/code/${code}`);
  }

  /**
   * Handles the getNextTransition workflow.
   */
  getNextTransition(payload: DecisionNextRequest): Observable<DecisionNextResponse> {
    return this.http.post<DecisionNextResponse>(`${this.baseUrl}/transitions/next`, payload);
  }
}
