import { Injectable } from '@angular/core';

/** localStorage key under which the JWT is persisted. */
const TOKEN_KEY = 'auth_token';

/**
 * TokenStorage (Frontend Infrastructure Lead).
 *
 * Single source of truth for persisting the JWT in the browser's localStorage.
 * The Authentication UI Engineer's AuthService calls setToken() after a successful login and
 * clear() on logout; the interceptor and route guard read it back here.
 */
@Injectable({ providedIn: 'root' })
export class TokenStorage {
  /** Returns the stored token, or null if the user isn't logged in. */
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  /** Persists the token (call after a successful login). */
  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  /** Removes the token (call on logout). */
  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
  }

  /** True when a token is present. */
  hasToken(): boolean {
    return this.getToken() !== null;
  }
}
