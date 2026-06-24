import { Component, inject, OnInit } from '@angular/core';
import { RegistrationService } from '../../services/registration-service';
import { User } from '../../models/user.model';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';

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
  isSubmitted = false;

  private formBuilder = inject(FormBuilder);

  private registrationService = inject(RegistrationService);

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

    console.log('Registration Data Successfully Submitted:', this.registerForm.value);

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
