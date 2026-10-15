-- =========================================================
-- Nursery CRM — MVP schema (Phase 0/1 scope)
--
-- Row-Level Security enforces tenant isolation. Every tenant-scoped
-- table gets a policy comparing tenant_id to the session-local
-- app.tenant_id, which the application sets per-transaction via
-- set_config(...). See backend/.../tenant/TenantAwareExecutor.java —
-- that file is the other half of this design; the RLS policies below
-- are useless if the app doesn't set app.tenant_id correctly on every
-- transaction, which is exactly the trap with connection pooling.
-- =========================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE tenants (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id   UUID NOT NULL REFERENCES tenants(id),
    email       TEXT NOT NULL,
    role        TEXT NOT NULL DEFAULT 'grower',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, email)
);

CREATE TABLE greenhouse_zones (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id    UUID NOT NULL REFERENCES tenants(id),
    name         TEXT NOT NULL,
    microclimate JSONB NOT NULL DEFAULT '{}'::jsonb -- target humidity/temp/light ranges
);

CREATE TABLE plant_batches (
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id          UUID NOT NULL REFERENCES tenants(id),
    species            TEXT NOT NULL,
    zone_id            UUID REFERENCES greenhouse_zones(id),
    propagation_date   DATE NOT NULL,
    quantity_initial   INTEGER NOT NULL,
    quantity_current   INTEGER NOT NULL,
    unit_cost          NUMERIC(10,2) NOT NULL DEFAULT 0,
    status             TEXT NOT NULL DEFAULT 'propagating'
                       CHECK (status IN ('propagating','growing','mature','sold','dead')),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Append-only log of everything that happens to a batch, including
-- events recorded offline. Never updated in place — this is what makes
-- offline sync safe: two conflicting field actions become two rows to
-- reconcile, not one row silently overwritten by whichever sync wins
-- the race. See frontend/lib/offlineSync.ts.
CREATE TABLE batch_events (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id    UUID NOT NULL REFERENCES tenants(id),
    batch_id     UUID NOT NULL REFERENCES plant_batches(id),
    event_type   TEXT NOT NULL, -- 'MORTALITY_RECORDED' | 'BATCH_MOVED' | 'TREATMENT_LOGGED' | ...
    payload      JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at  TIMESTAMPTZ NOT NULL, -- captured client-side; may predate synced_at
    synced_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    recorded_by  UUID REFERENCES users(id)
);

CREATE TABLE treatments (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id         UUID NOT NULL REFERENCES tenants(id),
    batch_id          UUID NOT NULL REFERENCES plant_batches(id),
    product_name      TEXT NOT NULL,
    treatment_type    TEXT NOT NULL CHECK (treatment_type IN ('organic','chemical')),
    applied_date      DATE NOT NULL,
    application_rate  TEXT,
    withholding_days  INTEGER NOT NULL,
    safe_ship_date    DATE GENERATED ALWAYS AS (applied_date + withholding_days) STORED
);

CREATE TABLE diagnostic_logs (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id        UUID NOT NULL REFERENCES tenants(id),
    batch_id         UUID NOT NULL REFERENCES plant_batches(id),
    symptom_notes    TEXT,
    photo_url        TEXT,
    cv_prediction    TEXT,
    cv_confidence    NUMERIC(4,3),
    needs_review     BOOLEAN NOT NULL DEFAULT true,
    reviewed_by      UUID REFERENCES users(id),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Time-series sensor data as a Timescale hypertable. Kept separate from
-- the retention/downsampling story of the relational tables above —
-- see ROADMAP.md, Phase 2.
CREATE TABLE sensor_readings (
    tenant_id    UUID NOT NULL REFERENCES tenants(id),
    zone_id      UUID NOT NULL REFERENCES greenhouse_zones(id),
    reading_time TIMESTAMPTZ NOT NULL,
    moisture     NUMERIC(5,2),
    humidity     NUMERIC(5,2),
    temperature  NUMERIC(5,2)
);
SELECT create_hypertable('sensor_readings', 'reading_time');

CREATE TABLE orders (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id     UUID NOT NULL REFERENCES tenants(id),
    customer_name TEXT NOT NULL,
    order_type    TEXT NOT NULL CHECK (order_type IN ('standard','contract_grow','wholesale')),
    status        TEXT NOT NULL DEFAULT 'pending',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE order_line_items (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id      UUID NOT NULL REFERENCES tenants(id),
    order_id       UUID NOT NULL REFERENCES orders(id),
    batch_id       UUID REFERENCES plant_batches(id), -- null until a contract-grow batch exists
    species        TEXT NOT NULL,
    promised_qty   INTEGER NOT NULL,
    promised_date  DATE
);

-- =========================================================
-- Row-Level Security
-- =========================================================

ALTER TABLE users              ENABLE ROW LEVEL SECURITY;
ALTER TABLE greenhouse_zones   ENABLE ROW LEVEL SECURITY;
ALTER TABLE plant_batches      ENABLE ROW LEVEL SECURITY;
ALTER TABLE batch_events       ENABLE ROW LEVEL SECURITY;
ALTER TABLE treatments         ENABLE ROW LEVEL SECURITY;
ALTER TABLE diagnostic_logs    ENABLE ROW LEVEL SECURITY;
ALTER TABLE sensor_readings    ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders             ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_line_items   ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON users
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);
CREATE POLICY tenant_isolation ON greenhouse_zones
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);
CREATE POLICY tenant_isolation ON plant_batches
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);
CREATE POLICY tenant_isolation ON batch_events
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);
CREATE POLICY tenant_isolation ON treatments
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);
CREATE POLICY tenant_isolation ON diagnostic_logs
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);
CREATE POLICY tenant_isolation ON sensor_readings
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);
CREATE POLICY tenant_isolation ON orders
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);
CREATE POLICY tenant_isolation ON order_line_items
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);

-- current_setting(..., true) returns NULL instead of raising when
-- app.tenant_id hasn't been set. Comparing tenant_id = NULL is never
-- true, so RLS fails CLOSED (zero rows) rather than erroring — the
-- safer default if the app ever forgets to set the context.
