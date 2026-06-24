import { Service, inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { User } from '../models/user';
import { catchError, Observable, of, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LoginService {
    private loginAPIURL = "http://localhost:8080/api/auth/login";
    private httpClient = inject(HttpClient);
    // string of JSON object containing username, id, token, and token expiration time
    private loginInfo: string | null = null;

    loginUser(user: User): Observable<any> {
        const headers = new HttpHeaders({ 
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*',
        });
        
        return this.httpClient.post<User>(this.loginAPIURL, user, { headers })
    }

    setLoginInfo(loginInfo: string) {
        this.loginInfo = loginInfo;
    }
}
