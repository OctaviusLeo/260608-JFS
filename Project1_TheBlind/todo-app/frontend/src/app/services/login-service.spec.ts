import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { LoginService } from './login-service';
import { RegistrationService } from './registration.service';
import { Token } from '../models/token.model';

describe('LoginService', () => {
  let service: LoginService;
  let registrationService: RegistrationService;
  const LOGIN_API_URL = 'http://localhost:8080/api/auth/login';
  const REGISTRATION_API_URL = 'http://localhost:8080/api/register';
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    service = TestBed.inject(LoginService);
    registrationService = TestBed.inject(RegistrationService);
    httpMock = TestBed.inject(HttpTestingController);

    // Register john_doe before each test so the user exists in the system
    const newUser = { username: 'john_doe', password: 'Abc**4' };
    registrationService.registerUser(newUser).subscribe();

    // Flush the registration request so it doesn't bleed into the test assertions
    const registerReq = httpMock.expectOne(REGISTRATION_API_URL);
    registerReq.flush({ username: 'john_doe', password: 'Abc**4' });
  });

  afterEach(() => {
    // Verify no unexpected requests were made after each test
    httpMock.verify();
  });

  // ── Successful login ───────────────────────────────────────────────────────

  it('should successfully log a user in if credentials match — returns 200 with a Token', () => {
    const credentials = { username: 'john_doe', password: 'Abc**4' };
    const mockToken: Token = {
      token: 'mock.jwt.token',
      expiresIn: 3600000,
      username: 'john_doe',
    };
    let result: Token | undefined;

    service.loginUser(credentials).subscribe({
      next: (token) => { result = token; },
    });

    const req = httpMock.expectOne(LOGIN_API_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(credentials);

    req.flush(mockToken); // simulate a 200 response with a JWT token body

    expect(result).toEqual(mockToken);
    expect(result?.token).toBe('mock.jwt.token');
  });

  // ── Unsuccessful login ─────────────────────────────────────────────────────

  it('should return a 400 error when the credentials do not match', () => {
    const wrongCredentials = { username: 'john_doe', password: 'WrongPass1!' };
    let errorStatus: number | undefined;

    service.loginUser(wrongCredentials).subscribe({
      error: (err) => { errorStatus = err.status; },
    });

    const req = httpMock.expectOne(LOGIN_API_URL);
    expect(req.request.method).toBe('POST');

    req.flush('Credentials do not match', { status: 400, statusText: 'Bad Request' });

    expect(errorStatus).toBe(400);
  });
});
