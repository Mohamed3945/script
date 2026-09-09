import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AppUser, RoleCode } from '../../../../core/models/auth.model';
import { UserApiService } from '../../../../core/services/user-api.service';

interface UserFormModel {
  id: number | null;
  username: string;
  password: string;
  displayName: string;
  email: string;
  active: boolean;
  roles: Record<RoleCode, boolean>;
}

@Component({
  selector: 'app-user-management-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-management-page.component.html',
  styleUrl: './user-management-page.component.scss'
})
export class UserManagementPageComponent implements OnInit {
  users: AppUser[] = [];
  loading = false;
  saving = false;
  errorMessage = '';
  passwordMessage = '';
  readonly roleCodes: RoleCode[] = ['SIMPLE', 'SUPER'];
  form: UserFormModel = this.createEmptyForm();

  constructor(private readonly userApi: UserApiService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.userApi.getUsers().subscribe({
      next: users => {
        this.users = users;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Impossible de charger les utilisateurs.';
        this.loading = false;
      }
    });
  }

  editUser(user: AppUser): void {
    this.errorMessage = '';
    this.passwordMessage = '';
    this.form = {
      id: user.id,
      username: user.username,
      password: '',
      displayName: user.displayName ?? '',
      email: user.email ?? '',
      active: user.active,
      roles: {
        SIMPLE: user.roles.includes('SIMPLE'),
        SUPER: user.roles.includes('SUPER')
      }
    };
  }

  resetForm(): void {
    this.form = this.createEmptyForm();
    this.errorMessage = '';
    this.passwordMessage = '';
  }

  saveUser(): void {
    this.errorMessage = '';
    const roles = this.selectedRoles();
    if (!roles.length) {
      this.errorMessage = 'Selectionnez au moins un role.';
      return;
    }
    if (!this.form.id && !this.form.password) {
      this.errorMessage = 'Le mot de passe est requis pour creer un utilisateur.';
      return;
    }

    this.saving = true;
    const request$ = this.form.id
      ? this.userApi.updateUser(this.form.id, {
          displayName: this.toNullable(this.form.displayName),
          email: this.toNullable(this.form.email),
          active: this.form.active,
          roles
        })
      : this.userApi.createUser({
          username: this.form.username.trim(),
          password: this.form.password,
          displayName: this.toNullable(this.form.displayName),
          email: this.toNullable(this.form.email),
          active: this.form.active,
          roles
        });

    request$.subscribe({
      next: savedUser => {
        this.upsertUser(savedUser);
        if (!this.form.id) {
          this.resetForm();
        } else {
          this.editUser(savedUser);
        }
        this.saving = false;
      },
      error: () => {
        this.errorMessage = 'Enregistrement impossible.';
        this.saving = false;
      }
    });
  }

  changePassword(): void {
    if (!this.form.id || !this.form.password) {
      this.passwordMessage = 'Saisissez un nouveau mot de passe.';
      return;
    }
    this.userApi.changePassword(this.form.id, { password: this.form.password }).subscribe({
      next: () => {
        this.form.password = '';
        this.passwordMessage = 'Mot de passe mis a jour.';
      },
      error: () => this.passwordMessage = 'Mise a jour du mot de passe impossible.'
    });
  }

  private createEmptyForm(): UserFormModel {
    return {
      id: null,
      username: '',
      password: '',
      displayName: '',
      email: '',
      active: true,
      roles: { SIMPLE: true, SUPER: false }
    };
  }

  private selectedRoles(): RoleCode[] {
    return this.roleCodes.filter(role => this.form.roles[role]);
  }

  private upsertUser(user: AppUser): void {
    const index = this.users.findIndex(item => item.id === user.id);
    if (index >= 0) {
      this.users = this.users.map(item => item.id === user.id ? user : item);
      return;
    }
    this.users = [...this.users, user];
  }

  private toNullable(value: string): string | null {
    const trimmed = value.trim();
    return trimmed ? trimmed : null;
  }
}