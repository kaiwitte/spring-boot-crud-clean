# Frontend

Angular app for the CRUD backend. All commands run from this directory.

## Prerequisites

The OpenAPI client in `src/generated/` is not committed. Generate it before
building or testing:

- ../gradlew openApiGenerateTypeScript

Then install dependencies once (and after dependency changes):

- npm install

## Everyday commands

- ng serve — dev server at http://localhost:4200, expects the backend on
  http://localhost:8080
- ng test — unit tests (vitest; watches in a terminal, single run when
  non-interactive)
- ng lint — eslint
- npm run format / npm run format:check — prettier

Without a global Angular CLI, prefix with npx or use the equivalent npm
scripts (npm start, npm test, npm run lint).

## Backend URL / environments

The API base URL comes from `src/environments/`:

- environment.development.ts — used by `ng serve`; points at
  http://localhost:8080. Change it here if your backend runs elsewhere.
- environment.ts — used by production builds (`ng build`); empty base
  path, i.e. API calls go to the same origin that serves the frontend.

The swap between the two happens via `fileReplacements` in `angular.json`.
Never hardcode a backend URL in application code.
