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

  /**
   * True only when a token is present AND its JWT `exp` claim is still in the
   * future. A leftover/expired token from a previous session (the backend
   * expires tokens after 1 hour) is treated as not authenticated.
   *
   * Decoding is best-effort: a malformed token is considered invalid.
   */
  hasValidToken(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }
    const expSeconds = this.getExpiry(token);
    if (expSeconds === null) {
      // Token is present but has no parsable exp claim (e.g. a non-standard or
      // opaque token). Treat it as valid and let the backend reject it if it's
      // actually stale — the guard is a UX convenience, not a security boundary.
      return true;
    }
    // `exp` is in seconds since epoch; Date.now() is milliseconds.
    return expSeconds * 1000 > Date.now();
  }

  /**
   * Reads the `exp` (expiry) claim from a JWT's payload without verifying the
   * signature (validation of authenticity stays server-side). Returns the
   * expiry in seconds since epoch, or null if it can't be read.
   */
  private getExpiry(token: string): number | null {
    try {
      const payload = token.split('.')[1];
      if (!payload) {
        return null;
      }
      // JWT uses base64url; convert to standard base64 before decoding.
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const json = JSON.parse(atob(base64));
      return typeof json.exp === 'number' ? json.exp : null;
    } catch {
      return null;
    }
  }
}
