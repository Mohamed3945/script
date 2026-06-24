import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ParameterOption } from '../models/parameter-option.model';

@Injectable({ providedIn: 'root' })
export class ParameterOptionApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getOptionsByDefinition(definitionId: number): Observable<ParameterOption[]> {
    return this.http.get<ParameterOption[]>(`${this.baseUrl}/parameter-definitions/${definitionId}/options`);
  }

  createOption(payload: ParameterOption): Observable<ParameterOption> {
    return this.http.post<ParameterOption>(`${this.baseUrl}/parameter-options`, payload);
  }

  updateOption(id: number, payload: ParameterOption): Observable<ParameterOption> {
    return this.http.put<ParameterOption>(`${this.baseUrl}/parameter-options/${id}`, payload);
  }

  deleteOption(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/parameter-options/${id}`);
  }
}