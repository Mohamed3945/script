import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthSessionService } from '../../../../core/services/auth-session.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss'
})
export class LoginPageComponent implements OnInit {
  username = '';
  password = '';
  loading = false;
  errorMessage = '';

  constructor(
    private readonly authSession: AuthSessionService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    if (this.authSession.isAuthenticated()) {
      this.router.navigateByUrl(this.returnUrl);
    }
  }

  login(): void {
    if (!this.username.trim() || !this.password) {
      this.errorMessage = 'Pseudo et mot de passe requis.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.authSession.login({ username: this.username.trim(), password: this.password }).subscribe({
      next: () => this.router.navigateByUrl(this.returnUrl),
      error: () => {
        this.loading = false;
        this.errorMessage = 'Identifiants invalides ou compte desactive.';
      }
    });
  }

  private get returnUrl(): string {
    return this.route.snapshot.queryParamMap.get('returnUrl') || '/';
  }
}