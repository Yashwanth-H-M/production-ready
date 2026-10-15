# Canopy Operations Cloud Roadmap

This roadmap is sequenced by risk and dependency rather than surface area. It records what is delivered in the current repository, what has a validated foundation but still needs production-completion work, and what remains intentionally deferred.

## Delivery status

| Phase | Status | Summary |
|---|---|---|
| Phase 0 — Foundation | **Substantially complete** | Tenant-aware backend, JWT signature verification, RLS hardening scripts, database roles, local Compose, security headers, and production deployment profile are delivered. |
| Phase 1 — Operational MVP | **Substantially complete** | Crop inventory, zones, treatments, diagnostics, orders, premium CRM interface, API gateway, PWA read cache, and core automated tests are delivered. |
| Production validation gap | **Open** | Hosted CI must pass the PostgreSQL RLS integration test with zero skips; production Clerk claims, cloud environment, and tenant-isolation smoke checks also need live verification before release. |
| Phase 2 — Intelligence and resilient field work | **Planned** | Offline write synchronization, sensor ingestion, trained diagnostic model, and authoritative label-source integration. |
| Phase 3 — Wholesale and scale | **Planned** | Advanced contract growing, dispatch, multi-region capacity, and tenant metering. |

## Phase 0 — Foundation

**Status: substantially complete.** The delivered foundation establishes the system boundary necessary for multi-tenant operational data.

| Item | Status | Delivered implementation |
|---|---|---|
| Verified token boundary | Complete | Local `JwtAuthFilter` handles development sessions; the production resource-server chain validates Clerk RS256 tokens and creates trusted principals. |
| Trusted tenant context | Complete | Tenant ID derives from verified JWT claims; conflicting caller header is rejected. |
| Transaction-safe tenant execution | Complete | `TenantAwareExecutor` scopes PostgreSQL `app.tenant_id` transaction-locally. |
| Server-derived ownership | Complete | `TenantOwnershipListener` assigns tenant IDs during persistence. |
| PostgreSQL RLS hardening | Complete | `03-security-hardening.sql` forces RLS and applies tenant defaults. |
| Least-privilege runtime role | Complete | `04-create-app-role.sh` creates the non-owner application database role. |
| Core data structures | Complete | Tenants, users, greenhouse zones, crop batches, events, treatments, diagnostics, and orders exist. |
| Local container stack | Complete | Docker Compose starts frontend, backend, database, and AI boundary. |
| PostgreSQL RLS integration proof | Implemented; hosted-CI execution pending | `PostgresRlsIntegrationTest` uses the TimescaleDB 2.18 / PostgreSQL 16 container, application role, forced RLS, tenant context, and append-only triggers. The GitHub Actions release workflow runs it on a hosted runner and rejects skips. |

The original Foundation exit criterion is deliberately not marked fully closed until the production database test proves two tenants remain isolated through RLS with the non-owner runtime role.

## Phase 1 — Operational MVP

**Status: substantially complete.** The current product can support the principal greenhouse operations workflow while retaining explicit human review for compliance and diagnostics.

| Item | Status | Delivered implementation |
|---|---|---|
| Living inventory | Complete | Crop batches support current quantity, lifecycle state, zone mapping, and cost fields. |
| Operational event history | Complete | Append-only batch events with recorder identity and database mutation protection. |
| Plant-health capture | Complete | Symptom notes, photo reference, advisory output fields, and review workflow. |
| Treatment compliance | Complete | Treatment records and safe-ship-date calculation with tested boundaries. |
| Customer commitments | Complete | Orders and line items with workspace-scoped relationship validation. |
| Sites and zones | Complete | Zone management and crop association. |
| Premium CRM UX | Complete | Canopy command center, responsive operations rail, command header, workspaces, and design system. |
| PWA/read-cache foundation | Complete | Manifest/icons and IndexedDB read-cache / queue primitives. |
| Production runtime profile | Complete | Hardened Docker images, Caddy TLS, private service network, health checks, and operator guide. |
| Authoritative automatic shipment release | Deferred | Requires a verified product-label source and approved policy. |
| Offline write synchronization | Deferred | Requires idempotency, ordering, reconciliation, and conflict UX. |

## Production completion work

The code has a production deployment path and core tenant controls. These remaining environment and validation steps must close before a public multi-tenant launch:

| Priority | Work item | Why it remains open |
|---|---|---|
| Critical | Run PostgreSQL RLS integration suite in hosted CI | H2 cannot prove real policy behavior or application-role restrictions. The hosted workflow must pass and report zero skipped integration tests before release. |
| Critical | Provision and verify production Clerk Organizations | Code integration is in place; production keys, session-token claims, organization onboarding, and end-to-end tenant-boundary checks remain unconfigured. |
| High | Durable database and recovery plan | Render Free Postgres is temporary (30 days) and has no managed backups. A paid durable database or explicit disposable-review policy plus tested exports is required. |
| High | Observability and alert routing | Operators need environment-specific logs, metrics, uptime checks, and escalation ownership. |
| High | Security review before public launch | Validate secret rotation, token audience/issuer policy, external exposure, and database role grants. |

## Phase 2 — Intelligence and resilient field work

**Status: planned.** Phase 2 adds higher-risk capabilities only after their data, audit, and human-control requirements are defined.

| Workstream | Planned result | Preconditions |
|---|---|---|
| Offline writes | Durable append-only field-event queue, ordered sync, conflict surfacing, and clear user status. | Idempotency design, reconciliation policy, user-session/tenant handling, integration tests. |
| Environmental telemetry | Sensor ingestion into the TimescaleDB foundation and rule-based threshold alerts. | Device protocol, retention policy, sensor ownership model, and alert review design. |
| Diagnostic assistance | Trained model integration that remains review-required. | Curated labeled data, evaluation criteria, monitoring, privacy/media governance. |
| Label-source integration | Authoritative product/withholding reference and auditable release workflow. | Reliable data source, legal/compliance review, jurisdiction policy, human override process. |

## Phase 3 — Wholesale and scale

**Status: planned.** Phase 3 expands commercial logistics and platform capacity after the core workflow and production controls have real usage evidence.

| Workstream | Planned result |
|---|---|
| Contract growing | Reserve prospective yield, model actual yield and mortality against commitments. |
| Wholesale dispatch | Dispatch manifests, route-aware grouping, and verified shipment controls. |
| Advanced analytics | Replace simple threshold alerts only after sufficient, governed historical data exists. |
| Platform scale | Multi-region architecture, measured tenant usage, and operational capacity planning. |

## Explicitly not claimed today

The product does not claim trained computer-vision accuracy, autonomous diagnostic approval, autonomous shipment release, durable offline write synchronization, sensor ingestion, fine-grained role authorization, a live production Clerk configuration, or a PostgreSQL RLS test run with zero skips. These are planned capabilities or production-completion items, not delivered facts.

See [ARCHITECTURE.md](ARCHITECTURE.md), [SECURITY.md](SECURITY.md), [TESTING.md](TESTING.md), and [DEPLOYMENT.md](DEPLOYMENT.md) for the current implementation boundary.
