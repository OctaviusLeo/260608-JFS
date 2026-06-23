import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  provideRouter,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';

import { authGuard } from './auth.guard';
import { TokenStorage } from './token-storage.service';

describe('authGuard', () => {
  let storage: TokenStorage;

  // The guard ignores its arguments, so empty snapshots are fine.
  const route = {} as ActivatedRouteSnapshot;
  const state = {} as RouterStateSnapshot;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([])],
    });
    storage = TestBed.inject(TokenStorage);
  });

  afterEach(() => localStorage.clear());

  it('allows activation when a token is stored', () => {
    storage.setToken('jwt-abc');

    const result = TestBed.runInInjectionContext(() => authGuard(route, state));
    expect(result).toBe(true);
  });

  it('redirects to /login when no token is stored', () => {
    const result = TestBed.runInInjectionContext(() => authGuard(route, state));

    expect(result).toBeInstanceOf(UrlTree);
    const expected = TestBed.inject(Router).createUrlTree(['/login']);
    expect((result as UrlTree).toString()).toBe(expected.toString());
  });
});
