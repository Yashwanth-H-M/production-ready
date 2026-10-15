# AGENTS.md

Read this guide before changing application code, infrastructure, schema scripts, or documentation. Pair it with [ARCHITECTURE.md](ARCHITECTURE.md), [SECURITY.md](SECURITY.md), [TESTING.md](TESTING.md), [ROADMAP.md](ROADMAP.md), and `database/schema.sql`.

## Project overview

Canopy Operations Cloud is a multi-tenant operations CRM for commercial plant nurseries and greenhouses. Its inventory model represents living, growing, mortal crop cohorts rather than static products. The product is production-hardened in its current architecture, while clearly documented planned features remain outside the delivered scope.

| Service | Stack | Responsibility |
|---|---|---|
| `backend/` | Java 21, Spring Boot 3.5, Spring Data JPA, Spring Security, JJWT | Tenant-aware REST API and domain validation. |
| `frontend/` | Next.js 16.4, React 19.2, TypeScript, Tailwind, ESLint 9 | Responsive CRM, PWA shell, same-origin API gateway, controlled offline reads. |
| `ai-service/` | Python 3.12, FastAPI | Advisory diagnostic service boundary; always review-required. |
| `database/` | PostgreSQL 16, TimescaleDB 2.18 | Tenant-scoped data, RLS, append-only events, sensor-ready storage. |
| Production edge | Caddy 2.8 | TLS termination and public forwarding to frontend only. |

## Required validation

```bash
# Full local stack
docker compose up --build

# Backend
cd backend && mvn test
cd backend && mvn spring-boot:run

# Production checks run on GitHub Actions; its hosted runner executes the
# PostgreSQL/TimescaleDB Testcontainers suite and fails if that test is skipped.

# Frontend
cd frontend && npm ci && npm run check
cd frontend && npm run clean && npm run build

# Compose manifests, from repository root (configuration checks only)
docker compose config
docker compose -f docker-compose.prod.yml config

# AI service
cd ai-service && python -m venv .venv
# Activate .venv in the active shell, then:
pip install -r requirements.txt && uvicorn main:app --reload
```

## Non-negotiable controls

Mistakes in these areas can create a tenant-data leak, a false compliance decision, or unreliable operational history. These are invariants, not style preferences.

1. **Every tenant-scoped query must use `TenantAwareExecutor`.** Do not call tenant-scoped repositories directly from a controller or service. The executor sets `app.tenant_id` transaction-locally using parameterized `set_config`, never concatenated SQL.
2. **Tenant identity comes from verified JWT claims.** Do not accept `tenantId` from JSON or use `X-Tenant-ID` to select a workspace. A conflicting header must be rejected.
3. **`TenantOwnershipListener` owns persisted tenant IDs.** New tenant records get their ownership from trusted context, not caller-controlled fields.
4. **`batch_events` are append-only.** Never add a mutation endpoint or work around the PostgreSQL update/delete rejection triggers. Corrections use compensating events.
5. **Safe-ship controls require human review.** Do not create an automated shipment-release path from `safe_ship_date` without an authoritative product-label source and separately approved policy.
6. **Diagnostics are advisory.** New diagnostics must preserve `needs_review=true`, regardless of prediction or confidence.
7. **The demo auth endpoint is never production authentication.** `LOCAL_DEMO_AUTH_ENABLED=false` in production. Do not weaken this switch or use local credentials in a public profile.
8. **The backend connects as the non-owner application role in production.** Never substitute the database owner credentials to solve an RLS or permission problem.

## Code and architecture conventions

| Area | Convention |
|---|---|
| Backend organization | Package by feature (`com.nurserycrm.<feature>`), with feature entity, repository, service, controller, and tests near each other. |
| Cross-cutting backend code | Keep authentication in `security`, tenant context/execution in `tenant`, and generic policy separate from feature packages. |
| Frontend components | Functional components. Reuse shell and operational primitives rather than copying structural markup. |
| Frontend data access | Use `frontend/lib/api.ts`; do not scatter raw fetch calls or browser-accessible backend URLs. |
| Offline behavior | Keep cache and queue mechanics in `frontend/lib/`; do not claim durable offline-write sync until reconciliation is implemented. |
| Schema evolution | `schema.sql` is the baseline. Once an environment exists, apply reviewed migrations; do not reset volumes to re-run initialization. |
| Documentation | Update the relevant contract documents with code changes, especially API, security, data model, frontend, testing, and deployment docs. |

## Canopy design system

Use the current branded system. Do not revive the legacy Nursery CRM or Verdant names, color palette, or typography.

| Token | Value | Use |
|---|---|---|
| Brand font | Manrope, weights 500–800 | Titles, navigation, decisive actions. |
| Body font | Inter | Supporting copy, forms, tables. |
| Data font | IBM Plex Mono | IDs, quantities, dates, compact metrics. |
| Canopy | `#0F7C61` | Primary trustworthy action/status. |
| Lime | `#D9FF5A` | Small high-attention accent. |
| Ink | `#18252A` | Operations rail and dark hierarchy. |
| App background | `#F5F7F8` | Content canvas. |

Prefer shared classes `e-panel`, `e-primary`, `e-secondary`, `e-input`, `e-select`, `e-table-head`, `e-table-cell`, `e-eyebrow`, and `e-title`. Preserve accessible focus states, clear status text, responsive mobile navigation, and purposeful animation.

## Testing expectations

Every new endpoint or tenant-sensitive feature needs a test that verifies failure across tenant boundaries as well as a happy path. Every change to safe-ship logic needs exact-boundary coverage. Frontend changes must pass `npm run check` and production build. Backend changes must pass `mvn test`.

H2 tests verify application behavior only. H2 cannot prove PostgreSQL row-level-security behavior. When implementing or changing RLS, application roles, row policies, or `app.tenant_id` behavior, add or update a true PostgreSQL integration test.

## Production constraints

Production is defined by `docker-compose.prod.yml`, Caddy TLS, private backend/database/AI-service networking, health checks, and `.env.production`. Read [DEPLOYMENT.md](DEPLOYMENT.md) before making a production change. Never commit real secrets, database dumps, customer data, media, or environment files.

## Commit and review guidance

Keep commits focused and describe the behavioral change. A pull request must include the relevant validation results and documentation update. Do not mark planned work complete simply because a stub, UI placeholder, or non-production test exists. Explicitly identify limitations such as missing PostgreSQL integration tests, identity-provider connection, offline write synchronization, or model training.
