import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { RegistrationService } from '../../services/registration.service';
import { User } from '../../models/user.model';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { delay, tap } from 'rxjs';

@Component({
  selector: 'app-registration-form',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './registration-form.component.html',
  styleUrl: './registration-form.component.css',
})
export class RegistrationForm {
  createdUser: User | null = null;
  errorMessage: string | null = null;
  registerForm!: FormGroup;
  isSubmitted: boolean = false;
  isRegistered: boolean = false;

  private formBuilder = inject(FormBuilder);

  private router = inject(Router);

  private registrationService = inject(RegistrationService);

  private cdr = inject(ChangeDetectorRef);

  private characterValidationRegex: RegExp = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=(?:.*[^A-Za-z0-9]){2}).+$/;

  ngOnInit(): void {
    this.registerForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(15), Validators.pattern(/^\S+$/)]],
      password: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(15), Validators.pattern(this.characterValidationRegex), Validators.pattern(/^\S+$/)]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  // Custom cross-field validator to match password fields
  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');
    
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
  }

  get f() {
    return this.registerForm.controls;
  }

  onSubmit(): void {
    this.isSubmitted = true;

    if (this.registerForm.invalid) {
      return;
    }

    // console.log('Registration Data Successfully Submitted:', this.registerForm.value);

    const newUser: User = {
      username: this.registerForm.value.username,
      password: this.registerForm.value.password,
    };

    this.registrationService.registerUser(newUser).pipe()
      // if successful, show success text on form, wait two seconds, and then navigate to login page
    .subscribe({
      next: (response) => {
        this.createdUser = response;
        this.isRegistered = true;
        this.cdr.detectChanges(); // force view update if response arrives outside Angular's zone
        // console.log('User created successfully:', response);
        setTimeout(() => this.router.navigate(['/login']), 2000); // brief pause so the user sees the success message
      },
      error: (error) => {
        // if unsuccessful, send alert about an error with server
        // this.errorMessage = 'Failed to create user.';
        // console.error('API Error:', error);
        this.isSubmitted = true;
        alert("There was an error with registering your information. Please try again.");
      }
    });
  }
}
