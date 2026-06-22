import { Component, inject } from '@angular/core';
import { RegistrationService } from '../../service/registration-service';
import { User } from '../../interface/user';

@Component({
  selector: 'app-registration',
  imports: [],
  templateUrl: './registration.html',
  styleUrl: './registration.css',
})
export class Registration {
  createdUser: User | null = null;
  errorMessage: string | null = null;

  private registrationService = inject(RegistrationService);

  onSubmit() {
    const newUser: User = {
      username: "john_doe",
      password: "Abc**4"
    };

    // Execute the POST request and handle response
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
