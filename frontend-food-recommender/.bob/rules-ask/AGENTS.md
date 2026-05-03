# AGENTS.md - Ask Mode

This file provides documentation context for this Angular SSR project.

## Project Structure (Non-Obvious)
- `src/app/` contains components WITHOUT "Component" suffix (e.g., `App` not `AppComponent`)
- Template files use `.html` extension (not `.component.html`)
- Style files use `.css` extension (not `.component.css`)
- No `src/app/components/` directory - components live directly in `src/app/`

## SSR Architecture
- **Two entry points**: [`src/main.ts`](../../src/main.ts:1) (browser) and [`src/main.server.ts`](../../src/main.server.ts:1) (server)
- Custom Express server in [`src/server.ts`](../../src/server.ts:1) (not standard Angular Universal)
- Server routes configured in [`app.routes.server.ts`](../../src/app/app.routes.server.ts:1) with prerendering
- Build outputs to TWO directories: `browser/` and `server/`

## Testing Setup
- Uses **Vitest** (not Karma/Jasmine) - Angular 21+ breaking change
- Test config in [`tsconfig.spec.json`](../../tsconfig.spec.json:1) with `vitest/globals` types
- Tests run via `ng test` command (wraps Vitest)

## Build System
- Uses new `@angular/build:application` builder (Angular 17+)
- `outputMode: "server"` enables SSR in [`angular.json`](../../angular.json:31)
- No separate `angular.json` for server config (unified build)

## State Management
- Uses Angular **signals** (not traditional properties or RxJS for component state)
- Signal syntax: `signal('value')` and accessed as `value()` in templates

## Deployment
- Server detects PM2 via `process.env['pm_id']` check
- Default port 4000 (not standard 4200)
- Serve command: `npm run serve:ssr:frontend-food-recommender`

## Styling System
- **Tailwind CSS** configured for utility-first styling
- Tailwind directives in [`src/styles.css`](../../src/styles.css:1)
- Config file: [`tailwind.config.js`](../../tailwind.config.js:1)
- Component `.css` files should be minimal (prefer Tailwind classes in templates)