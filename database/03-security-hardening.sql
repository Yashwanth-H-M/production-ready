-- Security hardening for fresh local databases.
-- This file runs after schema.sql and seed.sql in Docker Compose.
-- For an existing database, review and apply this script through the approved
-- migration process before enabling the application changes that depend on it.

-- Tenant IDs are derived from the transaction-local context set by
-- TenantAwareExecutor. JPA entities deliberately do not accept tenant_id
-- from API requests, preventing callers from choosing their owning tenant.
ALTER TABLE users              ALTER COLUMN tenant_id SET DEFAULT (current_setting('app.tenant_id', true)::uuid);
ALTER TABLE greenhouse_zones   ALTER COLUMN tenant_id SET DEFAULT (current_setting('app.tenant_id', true)::uuid);
ALTER TABLE plant_batches      ALTER COLUMN tenant_id SET DEFAULT (current_setting('app.tenant_id', true)::uuid);
ALTER TABLE batch_events       ALTER COLUMN tenant_id SET DEFAULT (current_setting('app.tenant_id', true)::uuid);
ALTER TABLE treatments         ALTER COLUMN tenant_id SET DEFAULT (current_setting('app.tenant_id', true)::uuid);
ALTER TABLE diagnostic_logs    ALTER COLUMN tenant_id SET DEFAULT (current_setting('app.tenant_id', true)::uuid);
ALTER TABLE sensor_readings    ALTER COLUMN tenant_id SET DEFAULT (current_setting('app.tenant_id', true)::uuid);
ALTER TABLE orders             ALTER COLUMN tenant_id SET DEFAULT (current_setting('app.tenant_id', true)::uuid);
ALTER TABLE order_line_items   ALTER COLUMN tenant_id SET DEFAULT (current_setting('app.tenant_id', true)::uuid);

-- The initialization role also serves the application locally. Table owners
-- otherwise bypass RLS, so force policy application even for that role.
ALTER TABLE users              FORCE ROW LEVEL SECURITY;
ALTER TABLE greenhouse_zones   FORCE ROW LEVEL SECURITY;
ALTER TABLE plant_batches      FORCE ROW LEVEL SECURITY;
ALTER TABLE batch_events       FORCE ROW LEVEL SECURITY;
ALTER TABLE treatments         FORCE ROW LEVEL SECURITY;
ALTER TABLE diagnostic_logs    FORCE ROW LEVEL SECURITY;
ALTER TABLE sensor_readings    FORCE ROW LEVEL SECURITY;
ALTER TABLE orders             FORCE ROW LEVEL SECURITY;
ALTER TABLE order_line_items   FORCE ROW LEVEL SECURITY;

-- Field events are immutable facts. Database protection keeps application bugs
-- and ad-hoc scripts from silently overwriting or removing offline records.
CREATE OR REPLACE FUNCTION reject_batch_event_mutation()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'batch_events is append-only; % is not allowed', TG_OP
        USING ERRCODE = '55000';
END;
$$;

CREATE TRIGGER batch_events_reject_update
    BEFORE UPDATE ON batch_events
    FOR EACH ROW EXECUTE FUNCTION reject_batch_event_mutation();

CREATE TRIGGER batch_events_reject_delete
    BEFORE DELETE ON batch_events
    FOR EACH ROW EXECUTE FUNCTION reject_batch_event_mutation();
