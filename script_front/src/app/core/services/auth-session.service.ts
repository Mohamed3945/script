import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

import { CurrentUser, LoginRequest, LoginResponse, RoleCode } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthSessionService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly baseUrl = 'http://localhost:8080/api/auth';
  private readonly tokenStorageKey = 'golden-script.auth.token';
  private readonly userStorageKey = 'golden-script.auth.user';
  private readonly currentUserSubject = new BehaviorSubject<CurrentUser | null>(this.readStoredUser());

  readonly currentUser$ = this.currentUserSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  get token(): string | null {
    if (!this.isBrowser()) {
      return null;
    }
    return sessionStorage.getItem(this.tokenStorageKey);
  }

  get currentUserSnapshot(): CurrentUser | null {
    return this.currentUserSubject.value;
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, request).pipe(
      tap(response => this.storeSession(response.token, response.user))
    );
  }

  loadCurrentUser(): Observable<CurrentUser> {
    return this.http.get<CurrentUser>(`${this.baseUrl}/me`).pipe(
      tap(user => this.storeUser(user))
    );
  }

  logout(): void {
    if (!this.isBrowser()) {
      this.currentUserSubject.next(null);
      return;
    }
    sessionStorage.removeItem(this.tokenStorageKey);
    sessionStorage.removeItem(this.userStorageKey);
    this.currentUserSubject.next(null);
  }

  isAuthenticated(): boolean {
    return !!this.token && !!this.currentUserSnapshot;
  }

  hasRole(role: RoleCode): boolean {
    return this.currentUserSnapshot?.roles?.includes(role) ?? false;
  }

  hasAnyRole(roles: RoleCode[]): boolean {
    return roles.some(role => this.hasRole(role));
  }

  private storeSession(token: string, user: CurrentUser): void {
    if (!this.isBrowser()) {
      this.currentUserSubject.next(user);
      return;
    }
    sessionStorage.setItem(this.tokenStorageKey, token);
    this.storeUser(user);
  }

  private storeUser(user: CurrentUser): void {
    if (!this.isBrowser()) {
      this.currentUserSubject.next(user);
      return;
    }
    sessionStorage.setItem(this.userStorageKey, JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  private readStoredUser(): CurrentUser | null {
    if (!this.isBrowser()) {
      return null;
    }
    const rawUser = sessionStorage.getItem(this.userStorageKey);
    if (!rawUser) {
      return null;
    }
    try {
      return JSON.parse(rawUser) as CurrentUser;
    } catch {
      sessionStorage.removeItem(this.userStorageKey);
      sessionStorage.removeItem(this.tokenStorageKey);
      return null;
    }
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }
}