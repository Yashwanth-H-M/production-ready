# Contributing Guide

## Purpose

Contributions to Canopy Operations Cloud must preserve tenant isolation, operational auditability, responsible review controls, and a consistent enterprise CRM experience. This guide applies to feature work, defect fixes, infrastructure changes, documentation, and test changes.

## Before you begin

Read [AGENTS.md](AGENTS.md), [ARCHITECTURE.md](ARCHITECTURE.md), [SECURITY.md](SECURITY.md), [DESIGN.md](DESIGN.md), and [TESTING.md](TESTING.md). Use the local Compose stack for changes that cross frontend, backend, database, or authentication boundaries.

| Change type | Required reading |
|---|---|
| Backend endpoint or entity | `API.md`, `DATA_MODEL.md`, `SECURITY.md`, `TESTING.md` |
| Tenant/RLS/authentication work | `SECURITY.md`, `backend/README.md`, tenant-isolation skill, database scripts |
| Frontend page or component | `FRONTEND.md`, `DESIGN.md`, `frontend/README.md` |
| Production/infrastructure work | `DEPLOYMENT.md`, `OPERATIONS.md`, production Compose and environment template |
| Schema change | `DATA_MODEL.md`, `SECURITY.md`, schema/hardening scripts, migration plan |
| Documentation-only change | The documentation index in `README.md` and the source feature contract |

## Development workflow

Create a focused branch, make the smallest coherent change, validate it, update the relevant contract documentation, and submit a pull request with evidence. Keep unrelated formatting, dependency upgrades, and refactors separate from operational behavior changes whenever practical.

| Step | Expectation |
|---|---|
| 1. Understand | State the user-facing and security impact before coding. |
| 2. Implement | Keep controller, service, data, UI, and infrastructure responsibilities separate. |
| 3. Test | Add or update the lowest appropriate automated test and run the project gate. |
| 4. Document | Update the feature contract, operational runbook, or user-developer guidance affected. |
| 5. Review | Describe validation results and any unclosed limitation in the pull request. |

## Coding standards

### Backend

Use package-by-feature organization. All tenant-scoped work must go through `TenantAwareExecutor`; tenant ownership must remain server-derived; related records must be fetched or validated in the current workspace; and batch events must remain append-only. Keep controllers focused on transport concerns and place business rules in services.

Do not accept tenant ownership, reviewer identity, event recorder identity, or automatic compliance approval as caller-controlled values. Add validation for numeric, temporal, and relationship constraints at the appropriate layer.

### Frontend

Use functional components, `AppShell`, shared operational UI primitives, and the shared API client. Create deliberate loading, empty, error, offline/cache, and pending-mutation states. Preserve semantic HTML, visible focus, responsive behavior, and plain-language operational messages.

Use the current Canopy identity: Manrope, Inter, IBM Plex Mono, canopy green `#0F7C61`, lime `#D9FF5A`, ink `#18252A`, and app background `#F5F7F8`. Do not reintroduce legacy naming, typography, or colors.

### Database and infrastructure

Treat `schema.sql` as the bootstrap baseline and plan a reviewed migration for existing environments. A tenant-owned table change requires RLS policy review, application-role grants review, service tenant-context review, and PostgreSQL integration-test coverage. Never solve access problems by running the application as the owner role.

## Required validation

| Scope | Command | Required outcome |
|---|---|---|
| Frontend | `cd frontend && npm run check` | No errors and no warnings. |
| Frontend build | `cd frontend && npm run clean && npm run build` | Production build completes. |
| Backend | `cd backend && mvn test` | Zero test failures. |
| Local Compose | `docker compose config` | Configuration validates. |
| Production Compose | `docker compose -f docker-compose.prod.yml config` | Configuration validates. |

Run the applicable commands locally and include concise results in the pull request. Manual acceptance evidence is expected when interaction, responsive layout, offline behavior, deployment, or visually meaningful changes are involved.

## Pull-request checklist

| Area | Required confirmation |
|---|---|
| Scope | The PR description explains user impact and does not hide unrelated changes. |
| Security | Tenant identity remains claim-derived; no secret or owner credential is introduced. |
| Data | Tenant-owned relations and append-only history rules remain correct. |
| Validation | Required automated checks passed, with results included. |
| Documentation | API, model, security, testing, deployment, and/or UI docs were updated as appropriate. |
| Accessibility | Keyboard, focus, label, error, and responsive behavior were considered. |
| Limitations | Any remaining gap is explicit rather than implied as complete. |

## Commit guidance

Use concise, imperative commit subjects that describe behavior, for example `Harden tenant role grants` or `Add treatment safe-ship validation`. Prefer focused commits that are easy to review and revert. Do not add unrelated generated artifacts, secrets, database dumps, personal configuration, or production customer data.

## Reporting defects and security issues

A defect report should state the expected outcome, actual outcome, reproduction steps, environment, and any safe screenshots or logs. Do not place secrets, tokens, or cross-tenant data in public issue text.

For a suspected tenant isolation, credential, or authentication problem, follow the security incident process in [SECURITY.md](SECURITY.md) and notify the repository owner privately. Preserve evidence without modifying append-only batch-event records.
