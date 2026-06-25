import { Component } from '@angular/core';
import { RegistrationForm } from '../../components/registration-form/registration-form.component';

@Component({
  selector: 'app-register-page',
  imports: [RegistrationForm],
  templateUrl: './register-page.html',
  styleUrl: './register-page.css',
})
export class RegisterPage {}
