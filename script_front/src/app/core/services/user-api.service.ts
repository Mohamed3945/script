import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { AppUser, ChangePasswordRequest, CreateUserRequest, UpdateUserRequest } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class UserApiService {
  private readonly baseUrl = 'http://localhost:8080/api/users';

  constructor(private readonly http: HttpClient) {}

  getUsers(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(this.baseUrl);
  }

  createUser(request: CreateUserRequest): Observable<AppUser> {
    return this.http.post<AppUser>(this.baseUrl, request);
  }

  updateUser(id: number, request: UpdateUserRequest): Observable<AppUser> {
    return this.http.put<AppUser>(`${this.baseUrl}/${id}`, request);
  }

  changePassword(id: number, request: ChangePasswordRequest): Observable<AppUser> {
    return this.http.put<AppUser>(`${this.baseUrl}/${id}/password`, request);
  }
}