export type RoleCode = 'SIMPLE' | 'SUPER';

export interface CurrentUser {
  id: number;
  username: string;
  displayName: string | null;
  email: string | null;
  active: boolean;
  roles: RoleCode[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: CurrentUser;
}

export interface AppUser {
  id: number;
  username: string;
  displayName: string | null;
  email: string | null;
  active: boolean;
  roles: RoleCode[];
}

export interface CreateUserRequest {
  username: string;
  password: string;
  displayName: string | null;
  email: string | null;
  active: boolean;
  roles: RoleCode[];
}

export interface UpdateUserRequest {
  displayName: string | null;
  email: string | null;
  active: boolean;
  roles: RoleCode[];
}

export interface ChangePasswordRequest {
  password: string;
}