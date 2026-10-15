# Testing Guide

## Testing objective

Canopy is validated across frontend static checks, backend domain tests, container configuration, and a production-server smoke path. The test strategy prioritizes the operational controls that must not regress: tenant ownership, crop quantity rules, safe-ship calculations, authenticated access boundaries, and deployable frontend/runtime configuration.

## Automated test layers

| Layer | Location or command | What it verifies |
|---|---|---|
| Frontend lint and TypeScript | `frontend: npm run check` | ESLint rules, TypeScript correctness, Next.js frontend conventions. |
| Frontend production build | `frontend: npm run clean && npm run build` | Static compilation, route assembly, and standalone production output. |
| Backend unit/service tests | `backend: mvn -Dtest='*Test,!PostgresRlsIntegrationTest' test` | Tenant context behavior, trusted ownership, JSON persistence behavior, batch validation, and safe-ship boundaries without starting local containers. |
| PostgreSQL/Timescale integration | GitHub Actions hosted runner via `Production checks` | Actual schema, non-owner role, forced RLS visibility and writes, transaction-local tenant scope, and append-only event triggers. The workflow fails if the test skips. |
| Compose configuration | `docker compose config` and production equivalent | Docker service composition and environment interpolation. |
| Production runtime smoke | Run the standalone frontend against a reachable backend | Startup and browser-visible route availability. |
| Production auth/CSP smoke | Hosted Vercel deployment with Clerk production configuration | Public production URL loads, Clerk sign-in and organization switching work, the document response contains a nonce-based strict CSP, and the browser reports no CSP violations. |

## Standard validation commands

Run these commands before opening a pull request. Resolve warnings as well as failures; the repository standard is zero frontend lint errors and zero warnings.

```bash
# Frontend static checks and production output
cd frontend
npm ci
npm run check
npm run clean
npm run build

# Backend unit/service suite; use the hosted workflow for the PostgreSQL/Timescale integration test
cd ../backend
mvn '-Dtest=*Test,!PostgresRlsIntegrationTest' test

# From repository root: validate local and production Compose manifests; these
# commands parse configuration and do not start Docker containers.
cd ..
docker compose config
docker compose -f docker-compose.prod.yml config
```

Do not start Docker on the developer workstation for release validation. Push the change to GitHub and use the hosted `Production checks` workflow, which runs the actual PostgreSQL/TimescaleDB integration suite with Testcontainers and rejects a skipped test.

The exact scripts available in a package are defined by that package's manifest or build file. Use the commands above as the standard project checks rather than substituting an unreviewed global tool invocation.

## Backend test coverage

| Test | Purpose | Current test environment |
|---|---|---|
| `TenantContextTest` | Verifies tenant context lifecycle and missing-context behavior. | H2/JUnit |
| `PlantBatchServiceTenantIsolationTest` | Verifies tenant ownership is server-derived and related records are checked in workspace scope. | H2/JUnit |
| `JsonFieldPersistenceTest` | Verifies zone profiles and field-event payloads persist as structured JSON and default to empty objects when omitted. | H2/JUnit; does not prove PostgreSQL JSONB behavior |
| `SafeShipDateTest` | Verifies three safe-ship-date boundary cases. | JUnit |
| `TenantOwnershipListenerTest` | Verifies trusted tenant context overrides a supplied ID and missing context is rejected. | JUnit |
| `ClerkJwtAuthenticationConverterTest` | Verifies stable tenant identity and rejects missing organization context. | JUnit |
| `PostgresRlsIntegrationTest` | Applies the actual Timescale schema and hardening SQL, seeds each tenant under its transaction-local context, connects as a non-owner role, and exercises cross-tenant CRUD, tenant and JSONB defaults, Hibernate JSONB round-trips, transaction-local context, and append-only events. | Testcontainers PostgreSQL 16 / TimescaleDB 2.18; run on the hosted CI runner |

The backend test suite is intentionally clear about its limits. H2 is used for fast service-level verification but does not emulate PostgreSQL row-level security or PostgreSQL-specific JSONB binding semantics. Passing H2 tests proves application-layer behavior, not database RLS policy enforcement or production driver compatibility.

The current frontend lockfile has nine `npm audit` findings in development-only tooling (seven high, two moderate); `npm audit --omit=dev` reports zero production dependency findings. The current findings are in Tailwind 3/PostCSS and Next ESLint tooling, and npm's suggested automatic fix would require a Tailwind major migration or an incompatible ESLint-config downgrade. Recheck the full audit when compatible upstream patches are available; do not treat the production-only audit as proof that the developer toolchain has no advisories.

## PostgreSQL integration test requirement

`PostgresRlsIntegrationTest` is implemented using the same TimescaleDB 2.18 / PostgreSQL 16 image and the repository's schema and hardening scripts. It verifies two-tenant isolation under a non-owner role, default ownership from transaction-local tenant context, fail-closed reads without context, database JSONB defaults, Hibernate JSONB round-trips, and append-only event mutation rejection. Run it through the hosted release workflow rather than starting local containers. The workflow checks the Surefire report and fails unless the test executes with zero skips. H2 alone does not validate RLS or production PostgreSQL JSONB behavior.

## Manual acceptance checks

Manual acceptance should use the isolated hosted review deployment and generated non-production data. Verify operator outcomes rather than only HTTP responses.

| Area | Acceptance scenario |
|---|---|
| Dashboard | Command center loads summary cards and decision queue without layout overflow. |
| Crop inventory | Create, update, and open a crop batch; invalid quantity states show a clear message. |
| Treatments | Log a treatment and confirm safe-ship date appears correctly. |
| Diagnostics | Create an observation; confirm it remains review-required, then mark it reviewed. |
| Batch references | In treatment and diagnostic forms, select a batch returned for the active workspace; confirm the form submits the batch UUID without manual ID entry. |
| Orders | Create a customer order and add a positive-quantity line item. |
| Sites and zones | Create, edit, and delete a growing zone within the current workspace. |
| Offline read state | With backend unavailable, verify cached reads show a clear stale/offline state rather than false success. |
| Mobile | Verify rail/nav and primary action paths at narrow viewport widths. |

Do not test tenant isolation by using client-provided `X-Tenant-ID` values. Test with tokens or seeded users whose verified tenant claims differ.

## Release gate

A release candidate must meet the following gate before deployment:

| Gate | Required result |
|---|---|
| Frontend check | No lint errors or warnings; TypeScript passes. |
| Frontend build | Production build completes successfully. |
| Dependency review | `npm audit` reviewed; no known production dependency vulnerabilities accepted without an explicit risk decision. |
| Backend tests | Maven test suite passes with zero failures. |
| Container config | Local and production Compose configurations validate. |
| Runtime | Standalone production frontend starts and serves the expected routes. |
| Security | Production demo auth is disabled; secrets are supplied; application DB role is non-owner. |
| Data isolation | Required GitHub Actions check passes and the PostgreSQL RLS integration test reports zero skips. |

## Test data handling

Use generated or seeded non-production data only. Do not copy commercial customer records, identifiable user data, or production diagnostic images into automated tests. Fixtures should explicitly include two tenants when a test concerns isolation.

See [SECURITY.md](SECURITY.md) for tenant-control requirements and [DEPLOYMENT.md](DEPLOYMENT.md) for production preflight checks.
