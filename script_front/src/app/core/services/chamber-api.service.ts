import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Chamber } from '../models/chamber.model';
import { ChamberDetail } from '../models/chamber-detail.model';

@Injectable({ providedIn: 'root' })
export class ChamberApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getMachineChambers(machineId: number): Observable<Chamber[]> {
    return this.http.get<Chamber[]>(`${this.baseUrl}/machines/${machineId}/chambers`);
  }

  getChamber(id: number): Observable<ChamberDetail> {
    return this.http.get<ChamberDetail>(`${this.baseUrl}/chambers/${id}`);
  }

  createChamber(machineId: number, payload: Chamber): Observable<Chamber> {
    return this.http.post<Chamber>(`${this.baseUrl}/machines/${machineId}/chambers`, payload);
  }

  updateChamber(id: number, payload: Chamber): Observable<Chamber> {
    return this.http.put<Chamber>(`${this.baseUrl}/chambers/${id}`, payload);
  }

  deleteChamber(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/chambers/${id}`);
  }

  addCapabilityToChamber(chamberId: number, capabilityId: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/chambers/${chamberId}/capabilities/${capabilityId}`, {});
  }

  removeCapabilityFromChamber(chamberId: number, capabilityId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/chambers/${chamberId}/capabilities/${capabilityId}`);
  }
}
