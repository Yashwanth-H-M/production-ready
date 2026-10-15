# Canopy Operations Cloud

Canopy Operations Cloud is a multi-tenant operations CRM for commercial nurseries and greenhouses. It treats crop inventory as living assets rather than static SKUs, combining crop-batch lifecycle management, plant-health review, treatment compliance, growing-zone context, and customer commitments in one operational workspace.

> **Product principle:** every operational record belongs to a verified tenant, crop events are durable facts, and high-impact recommendations remain subject to human review.

## Product capabilities

| Workspace | Operational purpose |
|---|---|
| Command center | Summarizes crop inventory, health reviews, compliance controls, and operational priorities. |
| Crop inventory | Manages plant batches through propagation, growth, maturity, sale, or loss. |
| Sites and zones | Records the physical growing locations and their environmental targets. |
| Compliance | Logs treatments and calculates safe-ship dates for withholding controls. |
| Plant health | Captures observations and maintains a human-review queue for diagnostic advice. |
| Revenue pipeline | Tracks customer orders and crop allocation commitments. |

## Architecture at a glance

| Layer | Technology | Responsibility |
|---|---|---|
| Frontend | Next.js 16, React 19, TypeScript, Tailwind CSS | Premium responsive CRM interface, controlled API gateway, offline read cache, and field workflows. |
| Backend | Java 21, Spring Boot 3.5, Spring Security, JPA | Tenant-aware REST API, business validation, JWT claim verification, and application workflows. |
| Database | PostgreSQL 16 with TimescaleDB | Tenant-scoped operational records, row-level security, append-only batch events, and future sensor data. |
| AI service | Python 3.12, FastAPI | Advisory diagnostic service boundary; recommendations remain reviewable. |
| Edge | Caddy in production | TLS termination and public routing to the frontend only. |

Read [ARCHITECTURE.md](ARCHITECTURE.md) for component boundaries and tenant data flow.

## Documentation index

| Document | Audience | Purpose |
|---|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Developers and architects | System components, tenant boundaries, data flow, and implementation choices. |
| [API.md](API.md) | Frontend and integration developers | API conventions, authentication model, endpoints, and error behavior. |
| [DATA_MODEL.md](DATA_MODEL.md) | Developers and data engineers | Core entities, relationships, lifecycle rules, and database constraints. |
| [DEVELOPMENT.md](DEVELOPMENT.md) | Contributors | Local setup, scripts, working conventions, and troubleshooting. |
| [TESTING.md](TESTING.md) | Developers and QA | Validation commands, test layers, and release checks. |
| [SECURITY.md](SECURITY.md) | Engineers and operators | Authentication, tenant isolation, data protection, and known security boundaries. |
| [OPERATIONS.md](OPERATIONS.md) | Operators | Service ownership, monitoring, backup, recovery, and incident procedures. |
| [DEPLOYMENT.md](DEPLOYMENT.md) | Operators | Render, Vercel, Clerk, database bootstrap, and deployment checklist. |
| [DESIGN.md](DESIGN.md) | Product and frontend developers | Canopy visual language, interaction guidance, and content voice. |
| [ROADMAP.md](ROADMAP.md) | Stakeholders | Completed foundation, current scope, and planned phases. |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Contributors | Pull-request expectations and code/documentation standards. |
| [CHANGELOG.md](CHANGELOG.md) | Stakeholders | Material project changes and release notes. |
| [database/README.md](database/README.md) | Database engineers | Initialization, roles, RLS, and safe schema evolution. |
| [ai-service/README.md](ai-service/README.md) | AI/service developers | Advisory service boundary and model-integration requirements. |

## Quick start

### Prerequisites

For local Compose development, install Docker Desktop with the Linux engine enabled. Release validation runs in GitHub Actions on a hosted runner; do not start Docker on a developer workstation for release checks. Service-by-service work uses Java 21 with Maven, Node.js 20.9 or later, and Python 3.12.

### Local stack

The default Compose file is intentionally for local development. It exposes service ports and enables the restricted demo session required for the seeded workspace.

```bash
docker compose up --build
```

After startup, open `http://localhost:3000`. The service endpoints are available on the local machine at ports `3000` (frontend), `8080` (backend), `5432` (database), and `8000` (AI service).

### Individual services

```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend
cd frontend
npm ci
npm run dev

# AI service
cd ai-service
python -m venv .venv
# Activate the virtual environment for your shell, then:
pip install -r requirements.txt
uvicorn main:app --reload
```

The frontend routes browser requests through `/api/*`. During local non-container development, the gateway defaults to `http://localhost:8080`; in Compose it uses the private backend service address.

## Quality commands

| Scope | Command |
|---|---|
| Frontend lint and types | `cd frontend && npm run check` |
| Frontend production bundle | `cd frontend && npm run clean && npm run build` |
| Frontend standalone server | `cd frontend && npm run start` |
| Backend tests | `cd backend && mvn test` |
| Compose syntax | `docker compose config` |
| Release checks including PostgreSQL RLS | GitHub Actions `Production checks` workflow |

## Production deployment

The cloud deployment path uses Clerk Organizations, Render for the Spring API and PostgreSQL/TimescaleDB, and Vercel for the Next.js frontend. A separate single-host Caddy production profile is also provided in `docker-compose.prod.yml`.

Read [DEPLOYMENT.md](DEPLOYMENT.md) before deploying. Render Free Postgres expires after 30 days and has no managed backups; it is suitable only for disposable academic review data, not durable production use. The deployment instructions list the remaining validation gates.

## Project status

The core product, tenant hardening, CRM interface, production frontend bundle, hosted release workflow, and deployment configuration are in place. Remaining release work is explicit: provision the cloud resources, run the hosted PostgreSQL RLS test with zero skips, verify a production Clerk app and token claims, and complete live tenant-isolation smoke checks. Render Free Postgres is temporary and is not durable production storage. Offline write synchronization, sensor ingestion, and trained diagnostic models remain planned. See [ROADMAP.md](ROADMAP.md) for scope and sequencing.

## License and ownership

This repository does not currently declare an open-source license. Treat it as proprietary project material unless the owner adds a license file.
