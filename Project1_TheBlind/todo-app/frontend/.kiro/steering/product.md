---
inclusion: always
---

# Product Overview

Frontend for a Todo application ("TheBlind" project). Provides a UI for managing tasks: creating, viewing, updating, and deleting todo items.

## Architecture

- **Type**: Single-page application (SPA)
- **Framework**: Angular 22 (standalone components, signals-based)
- **Language**: TypeScript (strict mode)
- **Testing**: Vitest with jsdom
- **Build**: Angular CLI (`@angular/build`)
- **Styling**: Plain CSS (component-scoped + global `styles.css`)
- **Package Manager**: npm

## Project Layout

The Angular app lives in `frontend/todo/`. Source code is in `frontend/todo/src/`.

```
frontend/todo/src/
├── app/
│   ├── app.ts              # Root component
│   ├── app.routes.ts       # Route definitions
│   ├── app.config.ts       # App-level providers
│   ├── app.html            # Root template
│   └── app.css             # Root styles
├── main.ts                 # Bootstrap entry point
├── index.html              # HTML shell
└── styles.css              # Global styles
```

## Authentication & User Management

- Users can register, log in, and log out
- Authorization uses JWTs issued by the backend
- Store the JWT securely (e.g., in-memory or `localStorage`) and attach it as a `Bearer` token in the `Authorization` header for every request to the `api/tasks` endpoint
- Use an Angular HTTP interceptor to inject the token automatically on protected routes
- Redirect unauthenticated users to the login page; guard task routes with an auth guard

## Backend Integration

- The frontend communicates with a separate backend API (sibling `backend/` folder)
- API calls should be isolated in dedicated service files under a `services/` directory
- Use Angular's `HttpClient` (via `provideHttpClient()`) for all HTTP communication
- All API calls must use RxJS Observables to keep the UI responsive during data fetching
- Use operators like `catchError`, `retry`, and `finalize` to manage request lifecycle
- Display user-friendly error messages based on HTTP status codes from the backend:
  - 401 → "Session expired. Please log in again."
  - 403 → "You don't have permission to perform this action."
  - 404 → "The requested resource was not found."
  - 500+ → "Something went wrong on our end. Please try again later."
  - Network error → "Unable to reach the server. Check your connection."
- Handle loading states explicitly in services (expose loading/error signals alongside data)

## Product Rules

- Every user-facing feature must support full CRUD operations on todo items
- The UI must remain responsive and accessible (WCAG AA compliance)
- Keep the app lightweight — stay within Angular CLI budget limits (500kB warning, 1MB error for initial bundle)
- Use lazy-loaded routes for feature areas to keep the initial bundle small
- All state should flow through Angular signals; avoid manual subscriptions where possible
