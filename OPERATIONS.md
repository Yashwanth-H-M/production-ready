# Operations Runbook

## Operational scope

This runbook describes the responsibilities required to operate a Canopy deployment built from `docker-compose.prod.yml`. It complements [DEPLOYMENT.md](DEPLOYMENT.md), which provides deployment steps. The deployment profile exposes only Caddy publicly; the frontend, backend, database, and AI service communicate on a private network.

## Service ownership and health

| Service | Operational purpose | Primary health signal | Publicly exposed |
|---|---|---|---|
| Caddy | TLS termination and request routing | HTTPS response and certificate validity | Yes, ports 80/443 |
| Frontend | Canopy UI and same-origin API gateway | User routes load; gateway can reach backend | Only through Caddy |
| Backend | Domain API and authentication boundary | `GET /health` returns `ok` | No |
| PostgreSQL/TimescaleDB | System of record and RLS enforcement | Database readiness, storage capacity, backup integrity | No |
| AI service | Advisory diagnostic boundary | Container/process health; no autonomous approval | No |

A healthy frontend alone is insufficient. Production health requires a working frontend route, backend health, database readiness, correct application-role access, valid TLS, and successful backup verification.

## Daily and release checks

| Check | Frequency | Expected outcome |
|---|---|---|
| HTTPS and certificate | Daily and before release | Public domain resolves over valid HTTPS. |
| Frontend command center | Daily and after release | Main application route renders without gateway errors. |
| Backend health | Daily and after release | `/health` reports `ok`. |
| Container status | Daily and after release | Required services are healthy and restart counts are understood. |
| Database capacity | Daily | Storage, connection use, and retention headroom remain within policy. |
| Backup job and restore sample | Per backup schedule | Backup completes and a recent restore is demonstrably usable. |
| Security configuration | Each release | Demo auth disabled, secrets supplied, non-owner application DB role used. |

## Logs and diagnosis

Use service logs to isolate a failure before taking corrective action. Avoid copying bearer tokens, unredacted environment variables, full customer records, or diagnostic media into tickets or chat channels.

```bash
# From the repository/deployment directory
docker compose --env-file .env.production -f docker-compose.prod.yml ps
docker compose --env-file .env.production -f docker-compose.prod.yml logs --tail=200 frontend
docker compose --env-file .env.production -f docker-compose.prod.yml logs --tail=200 backend
docker compose --env-file .env.production -f docker-compose.prod.yml logs --tail=200 db
```

| Symptom | First response |
|---|---|
| HTTPS unavailable | Check DNS, Caddy logs, public firewall rules, certificate issuance, and upstream frontend health. |
| UI loads but API requests return `503` | Check backend container health, backend `/health`, private network resolution, and gateway configuration. |
| `401` responses | Verify identity-provider token issuance/configuration; confirm production demo auth remains disabled. |
| Empty tenant-scoped lists | Confirm verified tenant claim, application role, RLS context, and server logs; do not change to owner credentials. |
| Database connection errors | Check database health, credentials, application-role grants, volume capacity, and connection settings. |
| Unusual container restart | Preserve logs, inspect configuration drift and resource pressure, then restart only after cause is understood. |

## Deployment change procedure

1. Review the change, linked tests, security impact, and documentation impact.
2. Ensure a recent successful backup exists and that recovery procedure has been tested within the defined operating policy.
3. Populate or verify the untracked `.env.production` values.
4. Validate the production Compose file.
5. Build and start the new stack according to [DEPLOYMENT.md](DEPLOYMENT.md).
6. Confirm public HTTPS, frontend routes, backend health, and a tenant-scoped authenticated workflow.
7. Monitor logs and container status through the defined observation window.
8. Record deployment version, date, operator, validation result, and any rollback trigger.

No destructive database migration, schema reset, or production volume deletion is permitted as a substitute for a planned rollback path.

## Backup and recovery

Database persistence is a production responsibility, not an automatic result of a named Docker volume. Establish a managed backup schedule for PostgreSQL, retain backups according to the organization's recovery objectives, encrypt backup storage, restrict access, and test restoration into an isolated environment.

| Recovery situation | Required response |
|---|---|
| Single-service issue | Restore the service from the known-good image/configuration; preserve data volumes. |
| Application deployment regression | Roll back to the prior tested application image/configuration if compatible with schema version. |
| Database corruption or loss | Stop writers, preserve evidence, restore a verified backup to a controlled target, validate data and RLS before cutover. |
| Credential compromise | Rotate the relevant credential, redeploy affected service configuration, invalidate compromised sessions where applicable, and review logs. |
| Suspected cross-tenant exposure | Follow security incident response; do not delete or alter evidence, including batch events. |

A restoration plan must account for schema version, RLS scripts, application-role grants, and compatible application images. Restoring data without restoring correct RLS/role configuration is not a successful recovery.

## Incident severity and escalation

| Severity | Example | Initial action |
|---|---|---|
| Critical | Suspected tenant data exposure, credential compromise, database loss, broad service outage | Contain access, preserve evidence, notify security and system owner immediately. |
| High | Production API unavailable, data writes failing, certificate expiry risk | Begin active diagnosis, assess impact, and prepare rollback or recovery. |
| Medium | A non-critical workspace feature degraded, elevated errors with workaround | Document impact, monitor, schedule safe remediation. |
| Low | Cosmetic issue or documentation inconsistency | Log for planned correction. |

## Current operational gaps

The supplied production profile contains health checks and restart policy but does not itself create managed monitoring, log aggregation, alert routing, secret-vault integration, database backup automation, or a configured identity provider. These must be selected, operated, and tested by the deployment owner before public service. The gaps are recorded in [ROADMAP.md](ROADMAP.md) rather than being hidden by the Docker configuration.

For deployment variables and container behavior, read [DEPLOYMENT.md](DEPLOYMENT.md). For data and security incident handling, read [SECURITY.md](SECURITY.md).
