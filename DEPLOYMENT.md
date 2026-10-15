# Canopy Operations Cloud — Production Deployment

## Supported deployment shape

The split deployment is a Vercel frontend, a Render Spring Boot backend, a Render PostgreSQL database with the `timescaledb` extension, and Clerk Organizations for sign-in and workspace membership. The separate FastAPI advisory service is not connected to current product flows and is intentionally not deployed as an active inference feature. The browser calls only the same-origin Next.js API gateway; that gateway adds the Clerk session token and forwards to the backend. The backend validates the token again and connects to PostgreSQL over Render's private network using the non-owner application role.

```text
Browser ── HTTPS ──> Vercel Next.js ── HTTPS + Clerk token ──> Render backend
                                                              │
                                                              └── private network ──> Render PostgreSQL/TimescaleDB
```

The existing production Compose profile is still available for a single-host Caddy deployment. It is independent of the Render/Vercel instructions below. Never use `docker-compose.yml` for public hosting; it is a local-development profile with demo authentication.

## Zero-cost database limitation

Render's Free Postgres is suitable for evaluating the deployment flow, but it is not a durable production database: it has a 1 GB limit, expires 30 days after creation, and has no Render-managed backups or connection pooling. Render explicitly says free instances are not for production. Do not store irreplaceable data there or represent that deployment as production durable. For a zero-cost professor review, use generated sample records only, export a `pg_dump` backup before the expiry, and plan a move to paid Render Postgres before real or persistent use. A free Render backend can also spin down while idle.

Render lists the `timescaledb` extension for PostgreSQL 13+, with Timescale Community-only features unavailable. The schema uses the basic hypertable API for `sensor_readings`; validate that exact schema against the created Render database before enabling the backend. The project currently has no live sensor-ingestion workflow, so do not claim one is operational. [Render free plan limits](https://render.com/docs/free), [Render PostgreSQL extensions](https://render.com/docs/postgresql-extensions), [Timescale feature matrix](https://docs.timescale.com/about/latest/timescaledb-editions/).

## Accounts and credentials

- GitHub: push the completed project to the new repository before either host can build it. The Render Blueprint expects an existing PostgreSQL resource named `canopy-postgres`.
- Render: create a PostgreSQL 16 database named `canopy-postgres` in Singapore on the Free plan. Then run the repository's manual database-bootstrap workflow before creating the API service. Free web/database resources cost $0, subject to usage limits; the Render account may require a payment method to create resources.
- Render: apply the root `render.yaml` Blueprint after the database bootstrap. It creates the backend Docker web service in Singapore and uses the private database host, port, and name. Render builds the backend image in the cloud; no developer workstation Docker daemon is needed.
- Vercel: create a project rooted at `frontend/` and import the repository. Use Vercel Hobby only for an eligible personal/non-commercial academic submission; check the current plan terms for your use.
- Clerk: create a production application and enable Organizations. In **Sessions → Customize session token**, add these claims:

  ```json
  {
    "aud": "canopy-api",
    "email": "{{user.primary_email_address}}",
    "org_name": "{{org.name}}"
  }
  ```

  Keep the default session token so session-bound claims are preserved. The backend accepts the active organization in either Clerk token v1 (`org_id`, `org_role`) or v2 (`o.id`, `o.rol`) format. Set the issuer and JWKS URL from the Clerk application. Restrict allowed organizations/membership per your submission needs.
- Keep all secret values in the provider's secret environment-variable interface. Never put credentials in Git, frontend `NEXT_PUBLIC_*` variables (except Clerk's publishable key), screenshots, or the repository.

## Database bootstrap

Create the Render database without loading demo seed data. Save the initial owner username/password before creating any other credentials. In Render's database **Credentials** section, add a managed PostgreSQL user named `canopy_app`; save its generated password. In the GitHub repository, create a protected environment named `render-database-bootstrap`, restrict it to the `main` branch, and require an owner approval before it can access secrets. Add these secrets from the Render dashboard: `RENDER_DB_EXTERNAL_HOST`, `RENDER_DB_EXTERNAL_PORT`, `RENDER_DB_NAME`, `RENDER_DB_OWNER_USER`, `RENDER_DB_OWNER_PASSWORD`, and the `canopy_app` password as `RENDER_APP_DB_PASSWORD`. Do not add the owner credential to the Render backend service. Run **Bootstrap fresh Render database** from GitHub Actions on `main` and confirm that the database is new and empty. The workflow grants the managed `canopy_app` user its least-privilege schema rights. It uses a hosted runner and TLS; it does not use Docker on the developer workstation.

The workflow applies the scripts in this order: `database/schema.sql`, `database/03-security-hardening.sql`, and `database/04-create-app-role.sh`. The role script grants the non-owner application account only application CRUD and sequence permissions. It then verifies the Timescale extension and `sensor_readings` hypertable. The baseline schema is not a migration: run this only once on a new empty database, never to reset an existing environment. Store `canopy_app` and the matching `RENDER_APP_DB_PASSWORD` in the Render service; never configure the backend with the database owner.

```sql
SELECT extversion FROM pg_extension WHERE extname = 'timescaledb';
SELECT hypertable_name FROM timescaledb_information.hypertables WHERE hypertable_name = 'sensor_readings';
```

If either query fails, stop and resolve compatibility before deploying the application. Do not apply the baseline schema over an existing database. Existing environments require a reviewed migration and a verified backup.

## Render backend configuration

Set the Render service root directory to `backend`, runtime to Docker, and Dockerfile path to `Dockerfile`. Set the health check path to `/health`. Add these environment variables in the Render dashboard:

| Variable | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `production` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<private-host>:5432/<database>?sslmode=require` using Render's **internal** database host |
| `SPRING_DATASOURCE_USERNAME` | Non-owner `APP_DB_USER` |
| `SPRING_DATASOURCE_PASSWORD` | Secret for `APP_DB_USER` |
| `AUTH_PROVIDER` | `clerk` |
| `CLERK_ISSUER` | Issuer URL from Clerk |
| `CLERK_JWKS_URL` | Clerk JWKS endpoint URL |
| `CLERK_API_AUDIENCE` | `canopy-api` |
| `LOCAL_DEMO_AUTH_ENABLED` | `false` |
| `TENANT_CONTEXT_SQL_ENABLED` | `true` |

Render's internal database hostname is reachable only to services in the same Render region. The backend connects over Render's private network; PostgreSQL is not exposed publicly. Only the backend should connect to the database. Render supplies `PORT` (default `10000`); Spring Boot binds to it in cloud and falls back to `8080` for local Compose.

## Vercel frontend configuration

Set the Vercel project root directory to `frontend`, use Node.js 22, and place serverless functions in Singapore (`sin1`) to match the Render backend region. Configure:

| Variable | Value |
|---|---|
| `NEXT_PUBLIC_AUTH_PROVIDER` | `clerk` (build and runtime) |
| `AUTH_PROVIDER` | `clerk` (runtime) |
| `NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY` | Clerk publishable key |
| `CLERK_SECRET_KEY` | Clerk secret key (server-only) |
| `API_INTERNAL_URL` | `https://<public-render-backend-host>` |

Vercel Authentication is configured for preview deployments only. Keep the production URL public so the professor can open it without a Vercel team account; Clerk still protects application routes and workspace data. Do not enable Vercel Authentication for all deployments unless you intend to add the professor to the Vercel team.

Clerk middleware applies a nonce-based strict Content-Security-Policy to hosted routes. Keep the Clerk provider's `dynamic` option enabled; it allows Next.js to propagate the per-request nonce to the rendered document.

The backend must be a public HTTPS web service because the Vercel server-side gateway cannot use Render's private network. Its API remains protected by Clerk bearer-token validation; only `/health` is unauthenticated. Do not set a browser-visible backend URL. Redeploy after setting or changing environment variables.

## First deployment and smoke checks

1. Create the PostgreSQL database and bootstrap it with the GitHub Actions workflow.
2. Set the Render Blueprint's `SPRING_DATASOURCE_USERNAME` to `canopy_app`, its password to `RENDER_APP_DB_PASSWORD`, and provide Clerk issuer/JWKS values before the backend becomes live.
3. Apply the Render Blueprint and confirm `https://<backend-host>/health` returns `ok`; this endpoint checks backend-to-database connectivity and returns `503` if the database is unavailable.
4. Configure the Clerk JWT claims and Vercel environment variables, then deploy the Vercel frontend.
5. Sign in, create/select an organization, verify that the API returns an empty workspace rather than demo records, and confirm the response has the nonce-based strict CSP with no browser CSP violations during sign-in or organization switching.
6. Create a minimal zone and batch through the UI, reload, and verify the records remain visible only in the same organization.
7. Verify unauthenticated API requests fail, a missing organization is rejected, and a second organization cannot read the first organization's records.
8. Export and restore a database backup before calling any non-disposable data durable.

The PostgreSQL RLS integration test is implemented and the `Production checks` workflow runs it on a hosted GitHub runner; the workflow fails if it is skipped. H2 tests cannot establish that the deployed database enforces the policies. Do not call the service production-ready until the hosted test, database bootstrap, Clerk configuration, and live smoke checks above pass.

## Backups and recovery

Free Render Postgres has no managed backups. For a temporary academic review database, regularly export from an operator machine and verify a restore into an isolated PostgreSQL/TimescaleDB instance:

```bash
pg_dump --dbname="$EXTERNAL_DATABASE_URL" --format=custom --no-owner --no-privileges > canopy-backup.dump
```

Do not commit the backup. Paid Render Postgres provides managed recovery features; configure and test the recovery window before storing real business data.

## Existing single-host Compose deployment

For the optional Caddy deployment, copy `.env.production.example` to `.env.production`, supply the owner and app-role credentials plus Clerk values, point DNS at the host, and open TCP 80/443. The frontend uses the internal backend address and Caddy is the only public ingress:

```bash
docker compose --env-file .env.production -f docker-compose.prod.yml up -d --build
```

On an empty volume, Compose loads `schema.sql`, hardens RLS, and creates the application role. Initialization scripts run only once for a fresh PostgreSQL data directory. Existing volumes require reviewed migrations, not volume deletion. In this profile, `JWT_SECRET` is not part of production authentication; Clerk is required.
