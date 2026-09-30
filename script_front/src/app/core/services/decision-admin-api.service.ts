import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { DecisionOption } from '../models/decision-option.model';
import {
  DecisionOptionAdminRequest,
  DecisionQuestionAdminRequest,
  DecisionResultProfile,
  DecisionResultProfileAdminRequest,
  DecisionTransitionAdmin,
  DecisionTransitionAdminRequest
} from '../models/decision-admin.models';
import { DecisionQuestion } from '../models/decision-question.model';

@Injectable({ providedIn: 'root' })
export class DecisionAdminApiService {
  private readonly baseUrl = `${environment.apiBaseUrl}/admin`;

  constructor(private readonly http: HttpClient) {}

  getQuestions(): Observable<DecisionQuestion[]> {
    return this.http.get<DecisionQuestion[]>(`${this.baseUrl}/questions`);
  }

  createQuestion(request: DecisionQuestionAdminRequest): Observable<DecisionQuestion> {
    return this.http.post<DecisionQuestion>(`${this.baseUrl}/questions`, request);
  }

  updateQuestion(id: number, request: DecisionQuestionAdminRequest): Observable<DecisionQuestion> {
    return this.http.put<DecisionQuestion>(`${this.baseUrl}/questions/${id}`, request);
  }

  deleteQuestion(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/questions/${id}`);
  }

  createOption(questionId: number, request: DecisionOptionAdminRequest): Observable<DecisionOption> {
    return this.http.post<DecisionOption>(`${this.baseUrl}/questions/${questionId}/options`, request);
  }

  updateOption(id: number, request: DecisionOptionAdminRequest): Observable<DecisionOption> {
    return this.http.put<DecisionOption>(`${this.baseUrl}/options/${id}`, request);
  }

  deleteOption(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/options/${id}`);
  }

  getTransitions(): Observable<DecisionTransitionAdmin[]> {
    return this.http.get<DecisionTransitionAdmin[]>(`${this.baseUrl}/transitions`);
  }

  createTransition(request: DecisionTransitionAdminRequest): Observable<DecisionTransitionAdmin> {
    return this.http.post<DecisionTransitionAdmin>(`${this.baseUrl}/transitions`, request);
  }

  updateTransition(id: number, request: DecisionTransitionAdminRequest): Observable<DecisionTransitionAdmin> {
    return this.http.put<DecisionTransitionAdmin>(`${this.baseUrl}/transitions/${id}`, request);
  }

  deleteTransition(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/transitions/${id}`);
  }

  getResultProfiles(): Observable<DecisionResultProfile[]> {
    return this.http.get<DecisionResultProfile[]>(`${this.baseUrl}/result-profiles`);
  }

  createResultProfile(request: DecisionResultProfileAdminRequest): Observable<DecisionResultProfile> {
    return this.http.post<DecisionResultProfile>(`${this.baseUrl}/result-profiles`, request);
  }

  updateResultProfile(id: number, request: DecisionResultProfileAdminRequest): Observable<DecisionResultProfile> {
    return this.http.put<DecisionResultProfile>(`${this.baseUrl}/result-profiles/${id}`, request);
  }

  deleteResultProfile(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/result-profiles/${id}`);
  }
}