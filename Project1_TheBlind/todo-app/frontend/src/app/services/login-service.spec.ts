import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { LoginService } from './login-service';
import { RegistrationService } from './registration.service';

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

    const newUser = { username: 'john_doe', password: 'Abc**4' };

    registrationService.registerUser(newUser).subscribe((user) => {
    });
  });

  // successful
  it('should successfully log a user in if credenntials match', () => {

  });

  // unsuccessful
  it('should return a 400 error when the credentials do not match', () => {
  });
});
