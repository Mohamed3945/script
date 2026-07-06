import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DecisionFinalizeRequest } from '../models/decision-finalize-request.model';
import { DecisionFinalizeResponse } from '../models/decision-finalize-response.model';

@Injectable({
  providedIn: 'root'
})
export class DecisionFinalizeApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  finalizeDecision(payload: DecisionFinalizeRequest): Observable<DecisionFinalizeResponse> {
    return this.http.post<DecisionFinalizeResponse>(
      `${this.baseUrl}/decision-executions/finalize`,
      payload
    );
  }
}
