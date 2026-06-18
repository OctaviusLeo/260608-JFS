---
inclusion: always
---

# Tech Stack & Coding Standards

## Versions

| Technology | Version | Notes |
|------------|---------|-------|
| Angular | 22 | Standalone components, signals-based reactivity |
| TypeScript | 6.0 | Strict mode — all strict flags enabled |
| RxJS | 7.8 | Used in services for async/HTTP flows only |
| Vitest | 4.x | Test runner with jsdom environment |
| Prettier | latest | Single formatting authority |
| npm | 11.x | Package manager — no yarn/pnpm |
| Angular CLI | latest | Build via `@angular/build` |

## Commands

All commands execute from `frontend/todo/`:

```bash
npm install          # Install dependencies
npm start            # Dev server (ng serve)
npm run build        # Production build
npm run watch        # Dev build with file watching
npm test             # Run unit tests (Vitest)
```

## TypeScript Rules

Strict compiler options — never disable these:

- `noImplicitOverride` — always use `override` keyword
- `noPropertyAccessFromIndexSignature` — use bracket notation for index signatures
- `noImplicitReturns` — every code path must return a value
- `noFallthroughCasesInSwitch` — require `break`/`return` in switch cases
- `isolatedModules` — each file must be self-contained
- Target: ES2022, Module: preserve

Angular compiler additions:

- `strictInjectionParameters` — DI parameter types must be explicit
- `strictInputAccessModifiers` — access modifiers on `@Input()` are enforced

## Formatting (Prettier)

Prettier is the only formatting tool. Do not use manual formatting or linter-based fixes.

- Max line width: 100 characters
- Quotes: single quotes (`'`)
- HTML parser: Angular (for `.html` template files)
- Apply with: `npx prettier --write .` from `frontend/todo/`

## Angular Patterns

### Component authoring

- **Standalone only** — never create or reference NgModules
- **Signals for state** — use Angular signals (`signal()`, `computed()`, `effect()`) for all component-level state
- **No manual subscriptions in components** — if you need an Observable in a component, convert it with `toSignal()` or use the `async` pipe
- **Dependency injection** — use the `inject()` function, never constructor injection
- **Component prefix** — always `app-` (e.g., `app-task-list`)

### Services

- **RxJS for HTTP** — services use `HttpClient` returning Observables
- **Error handling** — use `catchError`, `retry`, `finalize` operators
- **Loading/error state** — expose signals (`loading`, `error`) alongside data signals
- **No `HttpClient` in components** — all HTTP calls live in service files

### Routing

- **Lazy loading** — use `loadComponent` for every route to stay within bundle budgets
- **Route guards** — place in `guards/` directory, use functional guards

### Providers

- Register app-wide providers in `app.config.ts` using `provideHttpClient()`, `provideRouter()`, etc.
- Never use `providers` arrays in component metadata for app-wide services

## Build Budgets

Production builds enforce these limits — do not exceed them:

| Scope | Warning | Error |
|-------|---------|-------|
| Initial bundle | 500 kB | 1 MB |
| Component styles | 4 kB | 8 kB |

Keep bundles small by lazy-loading routes and avoiding large third-party imports in the main bundle.

## Backend Integration

- API backend is in the sibling `backend/` folder
- Use `HttpClient` via `provideHttpClient()` — never use `fetch()` directly
- All HTTP logic lives in `services/` — components consume services, never `HttpClient`
- Attach JWT via an HTTP interceptor — do not manually set headers in service methods
