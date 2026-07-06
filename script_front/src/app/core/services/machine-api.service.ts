import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Machine } from '../models/machine.model';
import { MachineDetail } from '../models/machine-detail.model';

@Injectable({ providedIn: 'root' })
export class MachineApiService {
  private readonly baseUrl = 'http://localhost:8080/api/machines';

  constructor(private http: HttpClient) {}

  getMachines(): Observable<Machine[]> {
    return this.http.get<Machine[]>(this.baseUrl);
  }

  getMachine(id: number): Observable<MachineDetail> {
    return this.http.get<MachineDetail>(`${this.baseUrl}/${id}`);
  }

  createMachine(payload: Machine): Observable<Machine> {
    return this.http.post<Machine>(this.baseUrl, payload);
  }

  updateMachine(id: number, payload: Machine): Observable<Machine> {
    return this.http.put<Machine>(`${this.baseUrl}/${id}`, payload);
  }

  deleteMachine(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
