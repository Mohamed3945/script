import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DecisionQuestion } from '../models/decision-question.model';
import { DecisionNextRequest } from '../models/decision-next-request.model';
import { DecisionNextResponse } from '../models/decision-next-response.model';

@Injectable({
  providedIn: 'root'
})
export class DecisionApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getEntryPointQuestion(): Observable<DecisionQuestion> {
    return this.http.get<DecisionQuestion>(`${this.baseUrl}/questions/entry-point`);
  }

  getQuestionById(id: number): Observable<DecisionQuestion> {
    return this.http.get<DecisionQuestion>(`${this.baseUrl}/questions/${id}`);
  }

  getQuestionByCode(code: string): Observable<DecisionQuestion> {
    return this.http.get<DecisionQuestion>(`${this.baseUrl}/questions/code/${code}`);
  }

  getNextTransition(payload: DecisionNextRequest): Observable<DecisionNextResponse> {
    return this.http.post<DecisionNextResponse>(`${this.baseUrl}/transitions/next`, payload);
  }
}