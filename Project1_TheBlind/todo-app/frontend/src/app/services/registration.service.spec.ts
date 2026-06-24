import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { RegistrationService } from './registration.service';

describe('RegistrationService', () => {
  const API_URL = 'http://localhost:8080/api/register';
  let service: RegistrationService;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    service = TestBed.inject(RegistrationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  // successful
  it('should return the created User on a successful registration', () => {
    const newUser = { username: 'john_doe', password: 'Abc**4' };
    const mockResponse: { username: string; password: string } = { username: 'john_doe', password: 'Abc**4' };
    let result: { username: string; password: string } | undefined;

    service.registerUser(newUser).subscribe((user) => {
      result = user;
    });

    const req = httpMock.expectOne(API_URL);
    req.flush(mockResponse); // simulate a 201 response with the created user body

    expect(result).toEqual(mockResponse);
  });

  // unsuccessful
  it('should return a 400 error when the username is less than 5 characters', () => {
    const shortUsernameUser = { username: 'john', password: 'Abc**4' };
    let errorStatus: number | undefined;

    service.registerUser(shortUsernameUser).subscribe({
      error: (err) => {
        errorStatus = err.status;
      },
    });

    const req = httpMock.expectOne(API_URL);
    req.flush('Username is not valid', { status: 400, statusText: 'Bad Request' });

    expect(errorStatus).toBe(400);
  });
});
