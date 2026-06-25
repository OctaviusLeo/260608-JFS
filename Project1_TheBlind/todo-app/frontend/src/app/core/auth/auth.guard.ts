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
  const tokenStorage = inject(TokenStorage);

  if (tokenStorage.hasValidToken()) {
    return true;
  }

  // No token, or a stale/expired one left over from a previous session.
  // Clear it so the interceptor doesn't keep sending a dead token, then
  // send the user to login.
  tokenStorage.clear();
  return router.createUrlTree(['/login']);
};
