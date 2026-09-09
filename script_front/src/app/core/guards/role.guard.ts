import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { RoleCode } from '../models/auth.model';
import { AuthSessionService } from '../services/auth-session.service';

export const roleGuard: CanActivateFn = route => {
  const authSession = inject(AuthSessionService);
  const router = inject(Router);
  const roles = (route.data['roles'] ?? []) as RoleCode[];

  if (!roles.length || authSession.hasAnyRole(roles)) {
    return true;
  }

  return router.createUrlTree(['/']);
};