---
inclusion: always
---

# Project Structure

## Source Root

The Angular application lives at `frontend/todo/`. All source code is under `frontend/todo/src/`.

```
frontend/todo/
├── src/
│   ├── app/
│   │   ├── app.ts             # Root standalone component
│   │   ├── app.html           # Root template
│   │   ├── app.css            # Root component styles
│   │   ├── app.config.ts      # Application providers (HttpClient, router, etc.)
│   │   ├── app.routes.ts      # Top-level route definitions
│   │   └── app.spec.ts        # Root component tests
│   ├── main.ts                # Bootstrap entry point
│   ├── index.html             # HTML shell
│   └── styles.css             # Global styles
├── public/                    # Static assets (favicon, images)
├── angular.json               # Angular CLI workspace config
├── tsconfig.json              # Base TypeScript config
├── tsconfig.app.json          # App-specific TS config
├── tsconfig.spec.json         # Test-specific TS config
└── package.json
```

## Target Directory Layout

As features are added, organise code inside `src/app/` using this structure:

```
src/app/
├── components/        # Shared, reusable UI components
├── pages/             # Routed page components (one per route)
├── services/          # Injectable services (API clients, state)
├── models/            # TypeScript interfaces and types
├── guards/            # Route guards (auth, role-based)
├── interceptors/      # HTTP interceptors (token injection, error handling)
├── pipes/             # Custom Angular pipes
└── utils/             # Pure helper functions
```

## Naming Conventions

| Artifact | Pattern | Example |
|----------|---------|---------|
| Component | `feature-name.ts` | `task-list.ts` |
| Template | `feature-name.html` | `task-list.html` |
| Styles | `feature-name.css` | `task-list.css` |
| Service | `feature-name.service.ts` | `task.service.ts` |
| Guard | `feature-name.guard.ts` | `auth.guard.ts` |
| Interceptor | `feature-name.interceptor.ts` | `token.interceptor.ts` |
| Model/Interface | `feature-name.model.ts` | `task.model.ts` |
| Test | `feature-name.spec.ts` | `task-list.spec.ts` |

Use kebab-case for file names. Use PascalCase for class names and camelCase for variables/functions.

## Structural Rules

- Use standalone components exclusively — no NgModules.
- Each routed feature gets its own folder under `pages/` containing its component, template, styles, and spec.
- Shared components used across multiple pages go in `components/`.
- API interaction logic belongs in `services/`, never in components directly.
- Co-locate component styles with their component file (same folder, same base name).
- Keep components focused on a single responsibility; extract logic into services or utils.
- Lazy-load feature routes via `loadComponent` in `app.routes.ts` to minimise the initial bundle.
- Place route guards in `guards/` and HTTP interceptors in `interceptors/`.
- Define data shapes as TypeScript interfaces in `models/` — do not use `any`.
