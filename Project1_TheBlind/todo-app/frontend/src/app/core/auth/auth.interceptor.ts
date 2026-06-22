import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { TokenStorage } from './token-storage.service';

/**
 * authInterceptor (Frontend Infrastructure Lead).
 *
 * Attaches `Authorization: Bearer <token>` to every outgoing request when a
 * token is stored, so feature services (e.g. TaskService) never set the header
 * themselves. Requests made before login simply pass through unchanged.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(TokenStorage).getToken();

  // No token yet (e.g. login/register calls) -> send the request as-is.
  if (!token) {
    return next(req);
  }

  // Requests are immutable, so clone with the auth header added.
  const authReq = req.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  });
  return next(authReq);

  // NOTE (Authentication UI Engineer): handle 401 responses (expired/invalid token) in AuthService.
};
