import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { AuthSessionService } from '../../../core/services/auth-session.service';

@Component({
  selector: 'app-top-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './top-navbar.component.html',
  styleUrl: './top-navbar.component.scss'
})
export class TopNavbarComponent {
  @Input() collapsed = false;
  @Output() toggleRequested = new EventEmitter<void>();

  private readonly authSession = inject(AuthSessionService);
  private readonly router = inject(Router);
  readonly currentUser$ = this.authSession.currentUser$;

  get isSuperUser(): boolean {
    return this.authSession.hasRole('SUPER');
  }

  requestToggle(): void {
    this.toggleRequested.emit();
  }

  logout(): void {
    this.authSession.logout();
    this.router.navigate(['/login']);
  }
}