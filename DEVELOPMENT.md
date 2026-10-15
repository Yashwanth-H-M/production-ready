# Development Guide

## Local development model

Canopy can run as a complete Docker Compose stack or as individual services. Use Docker Compose when working across frontend, backend, database, and AI-service boundaries. Use individual processes when changing a focused service and you already have compatible dependencies available.

| Service | Local Compose port | Primary development command |
|---|---:|---|
| Frontend | `3000` | `cd frontend && npm run dev` |
| Backend | `8080` | `cd backend && mvn spring-boot:run` |
| PostgreSQL | `5432` | `docker compose up db` or full Compose |
| AI service | `8000` | `cd ai-service && uvicorn main:app --reload` |

## Prerequisites

Install Docker Desktop with Linux containers for full-stack development. Individual-service work requires Java 21, Maven, Node.js 20.9 or later, npm, and Python 3.12. Git should be configured to preserve LF line endings for shell scripts and configuration files.

```bash
git clone <repository-url>
cd nursery-crm

docker compose up --build
```

Open `http://localhost:3000` after all services are healthy. The local Compose profile is deliberately not production-equivalent: it publishes developer ports and may enable a restricted local demo session for seeded workspace access.

## Service-by-service setup

### Frontend

```bash
cd frontend
npm ci
npm run dev
```

The frontend is a Next.js 16 application. Browser requests use the same-origin gateway at `app/api/[...path]/route.ts`, so new client code should call the shared API client rather than hard-coding a separate browser backend origin.

### Backend

```bash
cd backend
mvn spring-boot:run
```

Use the Spring configuration appropriate to the environment. Backend feature work must retain the tenant-aware service pattern; do not issue tenant-scoped repository calls outside `TenantAwareExecutor`.

### AI service

```bash
cd ai-service
python -m venv .venv
# Activate the environment for your shell.
pip install -r requirements.txt
uvicorn main:app --reload
```

The current AI boundary is intentionally advisory and returns review-required output. Do not turn it into autonomous crop, compliance, or shipping authorization without a separately reviewed safety and data-governance design.

## Working conventions

| Area | Required practice |
|---|---|
| Tenant data | Derive tenant identity from verified claims; never trust a client `tenantId` field or `X-Tenant-ID` header. |
| Database access | Use `TenantAwareExecutor` for every tenant-scoped operation. |
| Entity ownership | Allow `TenantOwnershipListener` to assign tenant ID on persistence. |
| Field history | Treat `batch_events` as append-only; use compensating events for corrections. |
| Shipment readiness | Preserve safe-ship-date controls and human review. |
| Diagnostics | Preserve `needs_review=true` on diagnostic creation. |
| Frontend calls | Use `frontend/lib/api.ts`; it centralizes token behavior, gateway access, and read-cache fallbacks. |
| Visual changes | Use the documented Canopy tokens and reusable `e-*` primitives. |

## Environment configuration

The root contains `.env.production.example` for production values. Do not create a committed `.env` containing credentials. Local Compose variables are defined by the Compose file and local environment. Production configuration belongs in an untracked `.env.production` file supplied at deploy time.

| Variable family | Purpose |
|---|---|
| `POSTGRES_*` | Owner role, database initialization, and database connection settings. |
| `APP_DB_*` | Least-privilege backend runtime database role. |
| `JWT_*` | Token validation configuration and local demo-signing controls. |
| `LOCAL_DEMO_AUTH_ENABLED` | Explicitly controls the development-only session endpoint. |
| `CADDY_*` | Public production domain and TLS routing configuration. |

## Common development tasks

### Add a backend endpoint

Begin with a tenant-aware service method, then expose a controller request/response contract. Validate all related IDs in the same workspace, keep tenant ID server-derived, add tests, update [API.md](API.md), and run `mvn test`.

### Add a frontend workspace

Add a route under `frontend/app`, use `AppShell` and the shared operational components, obtain data through the shared API client, provide loading/empty/error states, and update [FRONTEND.md](FRONTEND.md) and [DESIGN.md](DESIGN.md) if a reusable pattern changes.

### Change the data model

Review the source schema and security hardening scripts together. A schema change affecting tenant-owned data must include RLS policy analysis, application-role grants, an upgrade/migration path, tests, and documentation updates. Initialization scripts alone do not safely migrate an existing production database.

## Troubleshooting

| Symptom | First checks |
|---|---|
| Frontend reports API unavailable | Confirm backend health at `http://localhost:8080/health`, then inspect gateway environment settings and container health. |
| `401` API response in local use | Confirm the local demo session is enabled only for development and the browser token is current. |
| Empty data unexpectedly | Confirm the verified tenant claim, tenant context creation, and use of `TenantAwareExecutor`; never bypass RLS by switching to the owner role. |
| Database schema did not change | Existing PostgreSQL volumes do not re-run initialization. Plan and apply a migration; do not delete production volumes. |
| Frontend lint/build differs between machines | Use `npm ci`, supported Node version, and the repository lockfile. |
| Backend test appears to prove RLS | H2 does not implement PostgreSQL RLS. Run the planned PostgreSQL integration profile for database-policy validation. |

## Before requesting review

Run the commands in [TESTING.md](TESTING.md), review the relevant feature documentation, and confirm that no secret, generated build artifact, database volume, or user data was added to the change set. Review [CONTRIBUTING.md](CONTRIBUTING.md) for pull-request expectations.
