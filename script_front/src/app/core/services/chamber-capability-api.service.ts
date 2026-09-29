import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ChamberCapability } from '../models/chamber-capability.model';

@Injectable({ providedIn: 'root' })
export class ChamberCapabilityApiService {
  private readonly baseUrl = `${environment.apiBaseUrl}/chamber-capabilities`;

  constructor(private http: HttpClient) {}

  getCapabilities(): Observable<ChamberCapability[]> {
    return this.http.get<ChamberCapability[]>(this.baseUrl);
  }

  getCapability(id: number): Observable<ChamberCapability> {
    return this.http.get<ChamberCapability>(`${this.baseUrl}/${id}`);
  }

  createCapability(payload: ChamberCapability): Observable<ChamberCapability> {
    return this.http.post<ChamberCapability>(this.baseUrl, payload);
  }

  updateCapability(id: number, payload: ChamberCapability): Observable<ChamberCapability> {
    return this.http.put<ChamberCapability>(`${this.baseUrl}/${id}`, payload);
  }

  deleteCapability(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
