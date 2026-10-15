# Frontend Guide

## Frontend purpose

The Canopy frontend is a responsive operations workspace built with Next.js 16, React 19, TypeScript, and Tailwind CSS. It is designed for fast decisions by greenhouse and nursery teams: information hierarchy is deliberate, critical operational actions remain clear, and the interface remains usable at narrow field-device widths.

## Application structure

| Area | Responsibility |
|---|---|
| `app/` | App Router pages, layouts, route handlers, and route-specific user interfaces. |
| `app/api/[...path]/route.ts` | Same-origin server-side gateway for backend API requests. |
| `components/` | Reusable shell, operational UI primitives, and feature components. |
| `lib/api.ts` | Authenticated client requests, response handling, and IndexedDB read-cache fallback. |
| `lib/offlineSync.ts` | IndexedDB cache and field-event queue primitives; write synchronization is planned work. |
| `public/` | PWA manifest and icons. |

## Routes and workflows

| Route | Workspace | Primary user goal |
|---|---|---|
| `/` | Command center | Understand portfolio condition, decision queue, and planning signals. |
| `/batches` | Crop inventory | Search, inspect, and manage crop batches. |
| `/batches/new` | Crop inventory | Create a crop batch. |
| `/diagnostics` | Plant health review | Triage review-required plant-health observations. |
| `/diagnostics/new` | Capture observation | Record a field observation. |
| `/orders` | Revenue pipeline | Monitor customer commitments. |
| `/orders/new` | Revenue pipeline | Create a customer order. |
| `/treatments` | Compliance register | Review applications and safe-ship controls. |
| `/treatments/new` | Compliance register | Log an application. |
| `/zones` | Sites and zones | Maintain physical growing locations. |

## Shell and component model

`AppShell` establishes the product-level frame: dark operations rail, command header, responsive navigation behavior, and the main content canvas. Workspace pages should remain focused on their operational use case and obtain shared layout, navigation, spacing, and responsive behavior from the shell.

| Primitive or pattern | Use |
|---|---|
| `AppShell` | Persistent navigation, command header, connectivity state, and mobile behavior. |
| `e-panel` | Group related operational data or actions in an elevated surface. |
| `e-primary` | Primary, decisive action such as creating a batch or logging a treatment. |
| `e-secondary` | Secondary or reversible action. |
| `e-input` / `e-select` | Form controls with consistent density, states, and focus treatment. |
| `e-table-head` / `e-table-cell` | Dense but readable operational table hierarchy. |
| `e-eyebrow` | Small contextual category label above a title. |
| `e-title` | Workspace or page title. |

New visual elements should first use these primitives. Add a new reusable component only when the behavior or composition occurs in more than one workspace; otherwise keep feature-specific views local to the route.

## Design tokens

| Token | Value | Intended use |
|---|---|---|
| Brand font | `Manrope` (500/600/700/800) | Page titles, navigation, primary actions, and product identity. |
| Body font | `Inter` | General prose, form labels, tables, and supporting UI. |
| Data font | `IBM Plex Mono` | Quantities, IDs, precise dates, and operational metrics. |
| Canopy green | `#0F7C61` | Primary brand action and trusted operational status. |
| Lime accent | `#D9FF5A` | High-attention highlights and select status emphasis. |
| Ink | `#18252A` | Dark operations rail and high-contrast structure. |
| App background | `#F5F7F8` | Low-noise content canvas. |

The visual system is intentionally restrained. Lime is a small, high-signal accent rather than a background substitute. Color never carries critical status without accompanying text, iconography, or structural grouping.

## API and client state

Pages should call the shared client in `lib/api.ts`. Local development may use the local demo-session endpoint. Production uses Clerk Organizations for sign-in and organization selection; the server-side gateway obtains the session token and forwards it to the backend, so no bearer token or backend URL is exposed to browser JavaScript. The client centralizes response/error semantics and may return cached reads from IndexedDB when the network route is unavailable; components must clearly distinguish cached or offline state from confirmed live success.

The route handler acts as a gateway, not as a second backend. It forwards requests to the Spring service and returns `503` for upstream unavailability. Do not add business decisions, tenant selection, or secret client-side backend URLs to frontend code.

| State | Required UI behavior |
|---|---|
| Loading | Preserve layout hierarchy and show a deliberate progress state. |
| Empty | Explain the operational next step and expose the relevant create action. |
| Error | State the problem in plain language and offer a safe retry path. |
| Offline cache | Mark data as cached/stale and avoid suggesting that writes were committed. |
| Backend unavailable | Show a retryable service-unavailable state from the gateway. |

## Forms and mutation workflow

Form pages validate immediate constraints in the UI for speed, but backend validation remains authoritative. Use clear labels, real HTML input types, inline error placement, and disabled/pending states that prevent accidental duplicate submissions. Never send `tenantId`, `recordedBy`, `reviewedBy`, or safe-ship decisions as caller-owned security facts.

Treatment and diagnostic forms load crop-batch choices through the authenticated API and submit the selected UUID. Do not ask operators to copy or invent a batch ID; the server still validates that the selected batch belongs to the authenticated workspace.

On successful mutation, refresh or reconcile the relevant data view. For batch events, remember that the record is immutable; a correction must be represented as a new compensating event rather than a silent edit interface.

## Responsive and accessibility requirements

The desktop rail collapses into mobile navigation at narrow widths. New pages must verify keyboard navigation, visible focus, label association, contrast, semantic heading order, button names, table responsiveness, and touch-friendly target sizing. Animation should clarify hierarchy and state transition rather than delay actions or obscure a failure state.

## PWA and offline boundary

The manifest and `icon-192.png` / `icon-512.png` provide installable application metadata. IndexedDB read caching and event-queue primitives are foundation work only. The current product does not claim durable offline write synchronization, conflict resolution, or automated retry of business mutations. New offline behavior must define idempotency, ordering, reconciliation, tenant/session expiration, and user-facing conflict handling before it is enabled.

For product voice and detailed visual rules, read [DESIGN.md](DESIGN.md). For API behavior, read [API.md](API.md). For all frontend checks, read [TESTING.md](TESTING.md).
