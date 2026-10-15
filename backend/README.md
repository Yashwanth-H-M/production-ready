# Canopy Backend

The backend is a Java 21 / Spring Boot 3.5 REST API for tenant-isolated nursery operations. It owns domain validation, trusted tenant context, persistence workflows, and exposure of crop inventory, events, treatments, diagnostics, zones, and orders.

## Run and validate

```bash
# Execute the service and unit suite
mvn test

# Run locally with the active Spring configuration
mvn spring-boot:run

# Build an executable artifact
mvn package
```

Use the repository-level Compose stack for the normal full-stack workflow. The backend health endpoint is `GET /health` and the local endpoint is normally reachable at `http://localhost:8080/health`.

## Package structure

| Package | Responsibility |
|---|---|
| `config` | Security configuration, HTTP request/response logging filter, and global API exception handling. |
| `identity` | Organization identity provisioning, user mapping, and concurrency-safe account synchronization. |
| `security` | JWT signature verification, authenticated principal construction, and protected-route configuration. |
| `tenant` | Trusted tenant context, `TenantAwareExecutor`, request filter, and server-derived ownership listener. |
| `plantbatch` | Crop-batch lifecycle and inventory validation. |
| `batchevent` | Append-only operational field history. |
| `treatment` | Treatment records and safe-ship-date calculation. |
| `diagnostic` | Plant-health capture and human-review workflow. |
| `greenhouse` | Growing-zone management. |
| `order` | Customer orders and crop line-item commitments. |

## API boundary

All JSON endpoints are under `/api`; [../API.md](../API.md) is the authoritative endpoint reference. Controllers should remain thin: validate transport input, construct domain input, delegate to a tenant-aware service, and map safe outcomes to HTTP responses. Tenant selection, database policy setup, and cross-workspace authorization must not live in the controller alone.

## Tenant and security invariants

| Invariant | Required implementation |
|---|---|
| JWT trust | `JwtAuthFilter` verifies token signature before constructing `TenantPrincipal`. |
| Tenant source | Tenant identity derives from verified token claims, never caller JSON or `X-Tenant-ID`. |
| Request scope | A conflicting tenant header is rejected; missing trusted tenant context prevents tenant-scoped work. |
| SQL scope | Every tenant-scoped operation goes through `TenantAwareExecutor`, which sets transaction-local `app.tenant_id`. |
| Write ownership | `TenantOwnershipListener` assigns `tenant_id` from trusted server context. |
| Database defense | PostgreSQL RLS, forced RLS, and the non-owner application role protect data access. |
| Event history | `batch_events` are append-only and database triggers reject mutation. |
| Review controls | Diagnostics are created with `needs_review=true`; compliance calculations remain human-review controls. |

Do not bypass a permission failure by changing backend configuration to the owner database role. The application role is intentionally non-owner so production requests remain subject to RLS.

## Tests

```bash
mvn test
```

| Test | Coverage | Limitation |
|---|---|---|
| `TenantContextTest` | Trusted tenant context lifecycle and missing context behavior. | H2-based service test. |
| `PlantBatchServiceTenantIsolationTest` | Server-derived ownership and same-workspace relationship checks. | It does not prove PostgreSQL RLS. |
| `TenantProvisioningServiceTest` | Identity provisioning, Clerk claim mapping, and multi-threaded race condition safety. | Unit test with Mockito. |
| `SafeShipDateTest` | Safe-ship calculation boundary behavior. | Does not validate external label data. |

H2 cannot emulate PostgreSQL row-level security. A real PostgreSQL integration suite using the non-owner application role and forced RLS remains required before broad public multi-tenant release.

## Add a feature safely

Start with the domain and service layer. Identify every tenant-owned relation, run through `TenantAwareExecutor`, obtain related records using current-workspace lookups, and allow ownership to be server-derived. Add validation and tests before exposing a controller endpoint. Update [../API.md](../API.md), [../DATA_MODEL.md](../DATA_MODEL.md), [../SECURITY.md](../SECURITY.md), and [../TESTING.md](../TESTING.md) when a contract changes.

## Configuration

The Compose profiles provide standard environment values. Production configuration is supplied through the untracked repository-root `.env.production` file, based on `.env.production.example`. The local demo session is explicitly development-only; production must set `LOCAL_DEMO_AUTH_ENABLED=false` and use a configured verified identity-provider flow.
