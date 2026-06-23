import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { TokenStorage } from './token-storage.service';

/**
 * authGuard (Frontend Infrastructure Lead).
 *
 * Protects routes such as /dashboard: if a JWT is stored the navigation is
 * allowed, otherwise the user is redirected to the login page.
 */
export const authGuard: CanActivateFn = () => {
  const router = inject(Router);

  if (inject(TokenStorage).hasToken()) {
    return true;
  }

  // Not authenticated -> send them to login instead.
  return router.createUrlTree(['/login']);
};
