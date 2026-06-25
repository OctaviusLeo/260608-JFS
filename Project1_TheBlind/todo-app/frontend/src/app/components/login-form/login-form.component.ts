import { Component, inject, OnInit, signal } from '@angular/core';
import { LoginService } from '../../services/login-service';
import { User } from '../../models/user.model';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TokenStorage } from '../../core/auth/token-storage.service';

@Component({
  selector: 'app-login-form',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login-form.component.html',
  styleUrl: './login-form.component.css',
})
export class LoginForm {
  existingUser: User | null = null;
  // Signal so the failure message renders immediately when set from the async
  // login callback (a plain property would only show on the next DOM event).
  readonly errorMessage = signal<string | null>(null);
  loginForm!: FormGroup;
  isSubmitted = false;

  private formBuilder = inject(FormBuilder);

  private loginService = inject(LoginService);

  private router = inject(Router);

  private tokenStorage = inject(TokenStorage);

  ngOnInit(): void {
    this.loginForm = this.formBuilder.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]],
    });
  }

  get f() {
    return this.loginForm.controls;
  }

  onSubmit(): void {
    this.isSubmitted = true;
    this.errorMessage.set(null);

    if (this.loginForm.invalid) {
      return;
    }

    console.log('Login Data Successfully Submitted:', this.loginForm.value);
    // Execute backend API service transmission layer here
    // Execute the POST request and handle response

    const existingUser: User = {
      username: this.loginForm.value.username,
      password: this.loginForm.value.password,
    };

    this.loginService.loginUser(existingUser).subscribe({
      next: (response) => {
        console.log('User logged in successfully:', response);
        // Persist the JWT so the auth guard/interceptor can read it back.
        // Without this the dashboard route guard bounces straight back to login.
        this.tokenStorage.setToken(response.token);
        // all login info (including token timer, username, and id) saved as string of JSON object
        this.loginService.setLoginInfo(response);
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.errorMessage.set('Invalid username or password. Please try again.');
        console.error('API Error:', error);
      }
    });
  };
}
