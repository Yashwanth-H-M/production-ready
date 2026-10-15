# Canopy Frontend

The frontend is a Next.js 16.3.1 and React 19.2.0 application for Canopy Operations Cloud. It provides the command center, crop inventory, compliance register, plant-health review, revenue pipeline, sites and zones, and responsive field workflows.

## Run and validate

```bash
# Install locked dependencies
npm ci

# Local development server
npm run dev

# Lint and type checks; expected result is zero warnings and zero errors
npm run check

# Build standalone production output
npm run clean
npm run build

# Start the production build
npm run start
```

Open `http://localhost:3000` during local development. For the full product, start the repository-root Compose stack so the frontend can reach the backend and database services.

## Architecture

| Location | Responsibility |
|---|---|
| `app/` | App Router layouts, workspace pages, and route-level UI. |
| `app/api/[...path]/route.ts` | Same-origin server-side gateway to the Spring backend. |
| `components/` | Shared application shell and operational presentation components. |
| `lib/api.ts` | Authenticated request client, response handling, and IndexedDB read-cache fallback. |
| `lib/offlineSync.ts` | IndexedDB cache and event-queue primitives. |
| `public/` | PWA manifest and application icons. |

Browser code should call the same-origin `/api/*` gateway using the shared API client. Do not expose an alternate backend URL or add tenant-selection headers from the browser. The gateway returns a controlled `503` when the backend is unavailable.

## Canopy visual system

Use the shared operational classes and tokens rather than creating a second visual language.

| Token or class | Standard |
|---|---|
| Brand font | Manrope (500/600/700/800) |
| Body and data fonts | Inter and IBM Plex Mono |
| Brand colors | Canopy `#0F7C61`, lime `#D9FF5A`, ink `#18252A`, app background `#F5F7F8` |
| Shared primitives | `e-panel`, `e-primary`, `e-secondary`, `e-input`, `e-select`, `e-table-head`, `e-table-cell`, `e-eyebrow`, `e-title` |
| Global frame | `AppShell` dark operations rail, command header, online/offline state, and mobile navigation |

Read [../DESIGN.md](../DESIGN.md) for visual and content rules and [../FRONTEND.md](../FRONTEND.md) for page/component architecture.

## Offline boundary

The product currently supports IndexedDB read caching and queue primitives only. Cached content must be identified as stale or offline; it is not evidence that a new mutation was committed. Reliable offline write synchronization, idempotency, ordering, reconciliation, and conflict resolution are planned Phase 2 work.

## Authentication behavior

The shipped shared API client currently bootstraps a local token by calling `POST /api/auth/local-session`, stores it in browser storage, and forwards it through the gateway. This is a local-development path only; the endpoint ignores a request body and is disabled in the production profile. Tenant identity always comes from verified backend token claims, never client-selected state.

Before public production use, replace the local-session bootstrap with the approved identity-provider sign-in and token-refresh flow. Do not interpret the presence of production gateway support as a completed production user-authentication experience.

## Contributor expectations

New pages should use `AppShell`, reuse shared operational primitives, call the shared API client, and provide designed loading, empty, error, and offline states. Validate desktop and narrow mobile layouts, keyboard accessibility, focus visibility, and disabled/pending form behavior. Update [../API.md](../API.md) and [../TESTING.md](../TESTING.md) when frontend contracts or behavior change.
