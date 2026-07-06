import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChamberConfiguration } from '../models/chamber-configuration.model';

@Injectable({ providedIn: 'root' })
export class ChamberConfigurationApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getChamberConfigurations(chamberId: number): Observable<ChamberConfiguration[]> {
    return this.http.get<ChamberConfiguration[]>(`${this.baseUrl}/chambers/${chamberId}/configurations`);
  }

  getChamberConfiguration(id: number): Observable<ChamberConfiguration> {
    return this.http.get<ChamberConfiguration>(`${this.baseUrl}/chamber-configurations/${id}`);
  }

  createChamberConfiguration(chamberId: number, payload: ChamberConfiguration): Observable<ChamberConfiguration> {
    return this.http.post<ChamberConfiguration>(`${this.baseUrl}/chambers/${chamberId}/configurations`, payload);
  }

  updateChamberConfiguration(id: number, payload: ChamberConfiguration): Observable<ChamberConfiguration> {
    return this.http.put<ChamberConfiguration>(`${this.baseUrl}/chamber-configurations/${id}`, payload);
  }

  deleteChamberConfiguration(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/chamber-configurations/${id}`);
  }
}
