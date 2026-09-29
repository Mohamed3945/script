import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ParameterGroup } from '../models/parameter-group.model';
import { ParameterScope } from '../models/parameter-scope.model';

@Injectable({ providedIn: 'root' })
export class ParameterGroupApiService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getGroups(stepType: ParameterScope): Observable<ParameterGroup[]> {
    const params = new HttpParams().set('stepType', stepType);
    return this.http.get<ParameterGroup[]>(`${this.baseUrl}/parameter-groups`, { params });
  }

  createGroup(payload: ParameterGroup): Observable<ParameterGroup> {
    return this.http.post<ParameterGroup>(`${this.baseUrl}/parameter-groups`, payload);
  }

  reorderGroups(stepType: ParameterScope, orderedGroupIds: number[]): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/parameter-groups/reorder`, {
      stepType,
      orderedGroupIds
    });
  }
}