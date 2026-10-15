# Canopy Database

## Purpose

The database layer uses PostgreSQL 16 with TimescaleDB 2.18. It is the durable source of truth for tenant-owned nursery operations and supplies an independent tenant-isolation enforcement layer through row-level security (RLS).

## Initialization files

| File | Purpose | When it runs |
|---|---|---|
| `schema.sql` | Extensions, domain tables, base RLS policies, and TimescaleDB hypertable. | New PostgreSQL data-directory initialization. |
| `03-security-hardening.sql` | Forces RLS, applies tenant ID defaults from transaction context, and protects batch events from update/delete. | New data-directory initialization after schema. |
| `04-create-app-role.sh` | Creates/grants the least-privilege runtime application role. | New data-directory initialization after schema hardening. |

Initialization scripts do not migrate an existing database volume. Production schema changes require a reviewed migration plan, tested backup, compatible application version, and rollback/recovery procedure.

## Tenant enforcement

Each tenant-owned table is RLS-enabled. The backend executes tenant-scoped work through `TenantAwareExecutor`, which sets `app.tenant_id` transaction-locally. RLS policies compare each row's `tenant_id` with the current setting. When the setting is absent, PostgreSQL's comparison fails closed and returns no tenant rows.

| Layer | Responsibility |
|---|---|
| `TenantFilter` | Derives tenant from verified token claims and sets the trusted request context. |
| `TenantAwareExecutor` | Sets transaction-local PostgreSQL tenant context using parameterized SQL. |
| RLS policy | Limits the visible tenant rows at the database layer. |
| Ownership listener | Assigns tenant ID for new rows from trusted server context. |
| Application role | Runs backend queries without table-owner RLS bypass. |

## Roles

| Role | Local name | Production source | Responsibility |
|---|---|---|---|
| Owner | `nursery_owner` in local Compose; `canopy_owner` in Render | `POSTGRES_USER` / Render database owner | Initialization and controlled schema administration. |
| Application | `nursery_app` in local Compose; Render-managed `canopy_app` credential | `APP_DB_USER` | Runtime backend database access. |

The backend must not use the owner role. The application role receives table and sequence permissions but is intentionally non-owner; this preserves forced RLS enforcement.

## Append-only event history

`batch_events` preserve operational facts such as movement, mortality, treatment logging, and lifecycle changes. The hardening script installs triggers that reject both updates and deletion. Corrections must use compensating events; mutating or removing historical events is not permitted.

## Data operations

Use the owner role only for controlled administration. Before any migration or incident recovery, take a verified backup and document the schema version, RLS policies, application-role grants, and deployed image version. After restore, verify all four—not just row counts—before resuming writes.

The backend `PostgresRlsIntegrationTest` uses the actual schema, forced RLS, non-owner application role, and transaction-local `app.tenant_id` context. The GitHub Actions `Production checks` workflow runs it on a hosted runner and fails if it is skipped; H2 tests do not validate PostgreSQL RLS semantics. The Render database is initialized through the protected GitHub Actions workflow described in [../DEPLOYMENT.md](../DEPLOYMENT.md).

Read [../DATA_MODEL.md](../DATA_MODEL.md) for entities, [../SECURITY.md](../SECURITY.md) for control design, [../DEPLOYMENT.md](../DEPLOYMENT.md) for production configuration, and [../OPERATIONS.md](../OPERATIONS.md) for backup/recovery responsibilities.
