import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { authInterceptor } from './core/auth/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    // Installs the zoneless change-detection scheduler. Without it (and with no
    // zone.js dependency) async callbacks such as HTTP responses never schedule
    // a render, so signal updates after login/add/load were silently dropped.
    provideZonelessChangeDetection(),
    provideRouter(routes),
    // HttpClient + the JWT interceptor so every request carries the bearer token.
    provideHttpClient(withInterceptors([authInterceptor])),
  ],
};
