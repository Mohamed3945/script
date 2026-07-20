import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ParameterGroup } from '../models/parameter-group.model';
import { StepType } from '../models/step-type.model';

@Injectable({ providedIn: 'root' })
export class ParameterGroupApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getGroups(stepType: StepType): Observable<ParameterGroup[]> {
    const params = new HttpParams().set('stepType', stepType);
    return this.http.get<ParameterGroup[]>(`${this.baseUrl}/parameter-groups`, { params });
  }

  createGroup(payload: ParameterGroup): Observable<ParameterGroup> {
    return this.http.post<ParameterGroup>(`${this.baseUrl}/parameter-groups`, payload);
  }

  reorderGroups(stepType: StepType, orderedGroupIds: number[]): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/parameter-groups/reorder`, {
      stepType,
      orderedGroupIds
    });
  }
}