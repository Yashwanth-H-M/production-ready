# Changelog

All notable changes to Canopy Operations Cloud are documented in this file. The project does not yet use formal semantic version tags; entries therefore describe the delivered production-readiness baseline.

## Production Hardening, Observability, and Licensing — October 2026

### Added

| Area | Change |
|---|---|
| Observability | Added HttpLoggingFilter logging method, path, remote IP, bearer token presence, duration, and status for all incoming API calls while filtering automated /health pings. |
| Exception handling | Enhanced ApiExceptionHandler with global fallback handler, structured JSON error payloads, and full SLF4J stack trace logging. |
| Logging levels | Configured detailed application (com.nurserycrm=DEBUG), security (DEBUG), and JDBC (DEBUG) logging in both standard and production profiles. |
| Identity testing | Added TenantProvisioningServiceTest verifying identity synchronization, organization claim mapping, and concurrent multi-thread provisioning safety. |
| Licensing | Added official MIT License (LICENSE) declaring open-source terms and copyright for Canopy Operations Cloud. |

### Changed

| Area | Change |
|---|---|
| Backend concurrency | Hardened TenantProvisioningService to execute atomic PostgreSQL INSERT ... ON CONFLICT (id) DO UPDATE queries within TenantAwareExecutor, eliminating duplicate key race conditions during concurrent workspace bootstrap. |
| API gateway proxy | Fixed Next.js app/api/[...path]/route.ts to buffer upstream body via arrayBuffer(), delete forwarded content-encoding and content-length headers, and prevent client decompression mismatch. |
| Frontend error resilience | Fixed ApiError prototype inheritance across transpiled client bundles and safeguarded api.ts response parsing to eliminate Unexpected end of JSON input errors. |


## Unreleased — Canopy production-readiness baseline

### Added

| Area | Change |
|---|---|
| Product experience | Delivered Canopy Operations Cloud command center and rebuilt crop inventory, compliance, plant health, revenue pipeline, and sites-and-zones workspaces. |
| Design system | Added the Manrope/Inter/IBM Plex Mono type system, canopy/lime/ink palette, dark operations rail, command header, mobile navigation, and reusable `e-*` operational primitives. |
| Backend security | Added JWT signature verification, verified-principal tenant context, server-derived tenant ownership, and tenant header mismatch rejection. |
| Database security | Added forced row-level security, tenant defaults, append-only batch-event triggers, and a non-owner application database role. |
| Deployment | Added production Compose profile, Caddy TLS edge, private service topology, health checks, restart policies, hardened container images, and production environment template. |
| Frontend resilience | Added the server-side API gateway with controlled backend-unavailable handling, IndexedDB read-cache foundation, and PWA icons/manifest updates. |
| Documentation | Added architecture, API, security, data-model, development, testing, frontend, operations, contribution, and updated product/design/roadmap guides. |

### Changed

| Area | Change |
|---|---|
| Product identity | Renamed the active product experience to **Canopy Operations Cloud**. |
| Frontend stack | Upgraded to Next.js 16.3.1, React 19.2.0, TypeScript 5.6+, Tailwind CSS 3.4, and ESLint 9. |
| Production startup | Corrected the standalone Next.js production start behavior and verified it with a browser smoke test. |
| Tenant executor | Removed the ambiguous `Runnable` overload to ensure deterministic tenant-aware execution. |
| Tenant configuration | Removed duplicate component registration to ensure correct tenant configuration bean initialization. |
| Tests | Updated H2 tenant-isolation coverage to verify server-derived ownership without claiming PostgreSQL RLS coverage. |

### Verified

| Check | Result |
|---|---|
| Frontend lint and type checking | Passed with zero errors and zero warnings. |
| Frontend production build | Passed; generated the expected route set. |
| Frontend dependency audit | Passed with zero reported vulnerabilities at the time of validation. |
| Backend Maven suite | Passed with six tests and zero failures. |
| Local and production Compose manifests | Validated successfully. |
| Standalone frontend smoke path | Passed in browser verification. |

### Known limitations and planned work

| Limitation | Status |
|---|---|
| PostgreSQL RLS integration test | Still required; H2 cannot prove real PostgreSQL policy enforcement. |
| Production identity-provider connection | Still required; local demo auth is disabled in production. |
| Offline write synchronization | Planned; current implementation supports read caching and queue primitives only. |
| Sensor ingestion and telemetry alerts | Planned. |
| Trained plant-health model | Planned; diagnostics remain advisory and review-required. |
| Authoritative label data and automated shipment release | Planned; safe-ship control remains subject to human review. |

See [ROADMAP.md](ROADMAP.md) for planned phases and [TESTING.md](TESTING.md) for the validation boundary.
