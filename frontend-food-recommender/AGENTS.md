# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Non-Obvious Project Patterns

### Testing
- Uses **Vitest** (not Karma/Jasmine) - Angular 21+ default
- Test files: `*.spec.ts` with `vitest/globals` types in [`tsconfig.spec.json`](tsconfig.spec.json:8)
- Run tests: `npm test` (uses `ng test` which invokes Vitest)

### Component Conventions
- Components named WITHOUT "Component" suffix (e.g., `App` not `AppComponent`)
- Template files use `.html` extension (e.g., [`app.html`](src/app/app.html:1) not `app.component.html`)
- Style files use `.css` extension (e.g., [`app.css`](src/app/app.css:1) not `app.component.css`)
- Standalone components with direct `imports` array (no NgModule)

### State Management
- Uses Angular **signals** for reactive state (e.g., `signal('value')` in [`app.ts`](src/app/app.ts:11))
- Access signal values with `title()` syntax in templates

### SSR Configuration
- Custom Express server in [`src/server.ts`](src/server.ts:1) (not standard Angular Universal)
- Server checks `process.env['pm_id']` for PM2 deployment detection
- Default port: 4000 (not 4200)
- SSR entry point: [`src/main.server.ts`](src/main.server.ts:1)
- Server routes config: [`app.routes.server.ts`](src/app/app.routes.server.ts:1) with `RenderMode.Prerender`

### Build Configuration
- Uses `@angular/build:application` builder (new in Angular 17+)
- `outputMode: "server"` in [`angular.json`](angular.json:31) enables SSR
- Build outputs to `dist/frontend-food-recommender/browser` and `dist/frontend-food-recommender/server`

### Code Style
- Single quotes enforced (Prettier + EditorConfig)
- 100 character line width
- 2-space indentation
- Strict TypeScript mode enabled

### Styling with Tailwind CSS
- **MANDATORY**: Use Tailwind utility classes for ALL HTML styling
- Tailwind configured in [`tailwind.config.js`](tailwind.config.js:1)
- Global styles in [`src/styles.css`](src/styles.css:1) with Tailwind directives
- Avoid writing raw CSS in `.css` files unless absolutely necessary
- Component-specific styles should use Tailwind classes in templates