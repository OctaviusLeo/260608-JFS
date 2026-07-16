import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable, Service } from '@angular/core';
import { User } from '../models/user.model';
import { catchError, Observable, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RegistrationService {
    private registerAPIURL = `${environment.apiBaseUrl}/api/auth/register`;
    private httpClient = inject(HttpClient);

    registerUser(user: User): Observable<User> {
        const headers = new HttpHeaders({ 
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*',
        });
        
        return this.httpClient.post<User>(this.registerAPIURL, user);
    }
}
