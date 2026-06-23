import { TestBed } from '@angular/core/testing';

import { TokenStorage } from './token-storage.service';

describe('TokenStorage', () => {
  let storage: TokenStorage;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    storage = TestBed.inject(TokenStorage);
  });

  afterEach(() => localStorage.clear());

  it('returns null and false when no token is stored', () => {
    expect(storage.getToken()).toBeNull();
    expect(storage.hasToken()).toBe(false);
  });

  it('persists and reads back a token', () => {
    storage.setToken('jwt-123');
    expect(storage.getToken()).toBe('jwt-123');
    expect(storage.hasToken()).toBe(true);
  });

  it('clears the stored token', () => {
    storage.setToken('jwt-123');
    storage.clear();
    expect(storage.getToken()).toBeNull();
    expect(storage.hasToken()).toBe(false);
  });
});
