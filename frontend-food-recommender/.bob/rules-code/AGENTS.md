# AGENTS.md - Code Mode

This file provides coding-specific guidance for this Angular SSR project.

## Component Creation (Non-Obvious)
- Name components WITHOUT "Component" suffix: `export class App` not `AppComponent`
- Template files: `component-name.html` (not `component-name.component.html`)
- Style files: `component-name.css` (not `component-name.component.css`)
- Use standalone components with `imports` array (no NgModule)

## State Management
- Use Angular signals: `protected readonly title = signal('value')`
- Access in templates with function syntax: `{{ title() }}`
- Avoid traditional property binding for reactive state

## SSR Considerations
- Server code in [`src/server.ts`](../../src/server.ts:1) uses Express
- API endpoints must be defined BEFORE Angular catch-all route
- Static files served from `dist/frontend-food-recommender/browser`
- Server listens on port 4000 by default (configurable via `PORT` env var)

## Testing with Vitest
- Test files: `*.spec.ts` in same directory as source
- Use `vitest/globals` types (no explicit imports needed)
- Run single test: `npm test -- path/to/file.spec.ts`
- Vitest config embedded in Angular build system

## TypeScript Strict Mode
- All strict flags enabled in [`tsconfig.json`](../../tsconfig.json:6)
- `noImplicitReturns`, `noFallthroughCasesInSwitch` enforced
- Must explicitly type function returns

## Code Style
- Single quotes only (enforced by Prettier and EditorConfig)
- 100 character line width
- No access to MCP or Browser tools in this mode

## Styling Requirements
- **MANDATORY**: Use Tailwind CSS utility classes for ALL HTML styling
- NO raw CSS in component `.css` files (use Tailwind classes in templates)
- Only write custom CSS in [`src/styles.css`](../../src/styles.css:1) if absolutely necessary
- Tailwind config: [`tailwind.config.js`](../../tailwind.config.js:1)