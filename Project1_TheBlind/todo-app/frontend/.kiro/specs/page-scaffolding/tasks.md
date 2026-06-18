# Implementation Plan: Page Scaffolding

## Overview

Create two skeleton page components (Login and Dashboard) with semantic HTML structure, and configure the Angular router with lazy-loaded routes and a root redirect. Components use Angular 22 standalone defaults, co-located files, and are tested with Vitest + TestBed.

## Tasks

- [x] 1. Create Login Page component
  - [x] 1.1 Create the LoginPageComponent files
    - Create directory `src/app/pages/login/`
    - Create `login-page.ts` with `@Component` decorator, selector `app-login-page`, templateUrl and styleUrl references
    - Create `login-page.html` with `<header>` containing `<h1>Login</h1>` and `<main>` containing placeholder text
    - Create `login-page.css` (empty stylesheet)
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 4.1, 4.3_

  - [ ]* 1.2 Write unit tests for LoginPageComponent
    - Create `login-page.spec.ts` in the same directory
    - Test that the component creates successfully via TestBed
    - Test that the template contains a `<header>` element
    - Test that the template contains a `<main>` element
    - Test that the header contains an `<h1>` with text "Login"
    - Test that main contains placeholder text
    - _Requirements: 1.1, 1.2, 1.3, 4.1, 4.3_

- [x] 2. Create Dashboard Page component
  - [x] 2.1 Create the DashboardPageComponent files
    - Create directory `src/app/pages/dashboard/`
    - Create `dashboard-page.ts` with `@Component` decorator, selector `app-dashboard-page`, templateUrl and styleUrl references
    - Create `dashboard-page.html` with `<header>` containing `<h1>Task Dashboard</h1>` and `<main>` containing placeholder text
    - Create `dashboard-page.css` (empty stylesheet)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 4.2, 4.4_

  - [ ]* 2.2 Write unit tests for DashboardPageComponent
    - Create `dashboard-page.spec.ts` in the same directory
    - Test that the component creates successfully via TestBed
    - Test that the template contains a `<header>` element
    - Test that the template contains a `<main>` element
    - Test that the header contains an `<h1>` with text "Task Dashboard"
    - Test that main contains placeholder text
    - _Requirements: 2.1, 2.2, 2.3, 4.2, 4.4_

- [x] 3. Configure route definitions
  - [x] 3.1 Update app.routes.ts with lazy-loaded routes
    - Add a root path redirect from `''` to `'login'` with `pathMatch: 'full'`
    - Add `/login` route using `loadComponent` with dynamic import of `LoginPageComponent`
    - Add `/dashboard` route using `loadComponent` with dynamic import of `DashboardPageComponent`
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

  - [ ]* 3.2 Write unit tests for route configuration
    - Create or update a routing spec file to verify the routes array contains expected entries
    - Test that navigating to `/` redirects to `/login`
    - Test that `/login` resolves to LoginPageComponent
    - Test that `/dashboard` resolves to DashboardPageComponent
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 4. Final checkpoint
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- The design has no Correctness Properties — all acceptance criteria are verified through example-based unit tests
- Angular 22 defaults to standalone components, so `standalone: true` is not set explicitly
- Use `styleUrl` (singular) per Angular 22 conventions

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "2.1"] },
    { "id": 1, "tasks": ["1.2", "2.2", "3.1"] },
    { "id": 2, "tasks": ["3.2"] }
  ]
}
```
