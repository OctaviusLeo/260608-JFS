import { Service, inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { User } from '../models/user.model';
import { Token } from '../models/token.model';
import { catchError, Observable, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LoginService {
    private loginAPIURL = `${environment.apiBaseUrl}/api/auth/login`;
    private httpClient = inject(HttpClient);
    private loginInfo: Token | null = null;

    loginUser(user: User): Observable<Token> {
        const headers = new HttpHeaders({ 
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*',
        });
        
        return this.httpClient.post<Token>(this.loginAPIURL, user, { headers })
    }

    setLoginInfo(loginInfo: Token) {
        this.loginInfo = loginInfo;
    }
}
