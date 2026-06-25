import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable, Service } from '@angular/core';
import { User } from '../models/user.model';
import { catchError, Observable, of, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RegistrationService {
    private registerAPIURL = "http://localhost:8080/api/register";
    private httpClient = inject(HttpClient);

    registerUser(user: User): Observable<User> {
        const headers = new HttpHeaders({ 
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*',
        });
        
        return this.httpClient.post<User>(this.registerAPIURL, user);
    }
}
