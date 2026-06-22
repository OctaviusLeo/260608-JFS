import { Component, inject, OnInit } from '@angular/core';
import { RegistrationService } from '../../service/registration-service';
import { User } from '../../interface/user';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-registration',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './registration.html',
  styleUrl: './registration.css',
})
export class Registration {
  createdUser: User | null = null;
  errorMessage: string | null = null;
  registerForm!: FormGroup;
  isSubmitted = false;

  private formBuilder = inject(FormBuilder);

  private registrationService = inject(RegistrationService);

  ngOnInit(): void {
    this.registerForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(4)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
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

    console.log('Registration Data Successfully Submitted:', this.registerForm.value);
    // Execute backend API service transmission layer here
    // Execute the POST request and handle response

    const newUser: User = {
      username: this.registerForm.value.username,
      password: this.registerForm.value.password,
    };

    this.registrationService.registerUser(newUser).subscribe({
      next: (response) => {
        this.createdUser = response;
        console.log('User created successfully:', response);
      },
      error: (error) => {
        this.errorMessage = 'Failed to create user.';
        console.error('API Error:', error);
      }
    });
  }
}
