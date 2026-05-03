# AGENTS.md - Plan Mode

This file provides architectural planning context for this Angular SSR project.

## Architecture Constraints (Non-Obvious)

### Component Architecture
- Components must be standalone (no NgModule pattern)
- Component naming WITHOUT "Component" suffix is the convention
- Template/style files use simple extensions (`.html`, `.css` not `.component.html`)
- Direct imports array in component decorator (no module declarations)

### SSR Architecture
- **Dual-mode rendering**: Browser + Server with separate entry points
- Express server in [`src/server.ts`](../../src/server.ts:1) handles SSR (not standard Angular Universal)
- API routes MUST be defined before Angular catch-all route (order matters)
- Static files served with 1-year cache from `browser/` directory
- Server routes in [`app.routes.server.ts`](../../src/app/app.routes.server.ts:1) control render mode per route

### State Management Pattern
- Angular signals are the primary state mechanism (not services with RxJS)
- Signals provide reactive state without subscriptions
- Pattern: `signal()` for creation, `()` for access in templates

### Testing Architecture
- Vitest replaces Karma/Jasmine (Angular 21+ default)
- Tests must be co-located with source files (not in separate `test/` directory)
- `vitest/globals` provides test functions without imports

### Build Pipeline
- Unified build system via `@angular/build:application` (Angular 17+)
- Single `angular.json` config for both browser and server builds
- `outputMode: "server"` triggers SSR build
- Two output directories: `dist/*/browser` and `dist/*/server`

### Deployment Considerations
- PM2 detection built into server startup logic
- Port 4000 default (not 4200) - configurable via `PORT` env var
- Server must be started separately: `npm run serve:ssr:frontend-food-recommender`
- Prerendering configured by default for all routes (`RenderMode.Prerender`)

### Styling Architecture
- **Tailwind CSS** is the mandatory styling system
- Utility-first approach - use Tailwind classes in HTML templates
- Avoid writing raw CSS in component `.css` files
- Global styles only in [`src/styles.css`](../../src/styles.css:1)
- Tailwind configured in [`tailwind.config.js`](../../tailwind.config.js:1) scanning `src/**/*.{html,ts}`