package com.nurserycrm.tenant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nurserycrm.batchevent.BatchEvent;
import com.nurserycrm.greenhouse.GreenhouseZone;
import com.nurserycrm.plantbatch.PlantBatch;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Path;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
class PostgresRlsIntegrationTest {
    private static final String APP_ROLE = "canopy_rls_test_app";
    private static final String APP_PASSWORD = "test-app-password";

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
        DockerImageName.parse("timescale/timescaledb:2.18.0-pg16").asCompatibleSubstituteFor("postgres"))
        .withDatabaseName("nursery_crm")
        .withUsername("canopy_owner")
        .withPassword("owner-test-password");

    private static UUID tenantA;
    private static UUID tenantB;
    private static UUID zoneA;
    private static UUID zoneB;
    private static UUID batchA;
    private static UUID eventId;

    @BeforeAll
    static void initializeProductionSchemaAndRole() throws Exception {
        tenantA = UUID.randomUUID();
        tenantB = UUID.randomUUID();
        zoneA = UUID.randomUUID();
        zoneB = UUID.randomUUID();
        batchA = UUID.randomUUID();
        eventId = UUID.randomUUID();

        try (Connection owner = ownerConnection()) {
            try (Statement statement = owner.createStatement()) {
                statement.execute(Files.readString(Path.of("..", "database", "schema.sql")));
                statement.execute(Files.readString(Path.of("..", "database", "03-security-hardening.sql")));
            }

            owner.setAutoCommit(false);
            try (PreparedStatement insertTenants = owner.prepareStatement(
                    "INSERT INTO tenants (id, name) VALUES (?, ?), (?, ?)")) {
                insertTenants.setObject(1, tenantA);
                insertTenants.setString(2, "Tenant A");
                insertTenants.setObject(3, tenantB);
                insertTenants.setString(4, "Tenant B");
                insertTenants.executeUpdate();
            }
            owner.commit();

            setTenant(owner, tenantA);
            try (PreparedStatement insertZone = owner.prepareStatement(
                    "INSERT INTO greenhouse_zones (id, tenant_id, name) VALUES (?, ?, ?)")) {
                insertZone.setObject(1, zoneA);
                insertZone.setObject(2, tenantA);
                insertZone.setString(3, "A zone");
                insertZone.executeUpdate();
            }
            try (PreparedStatement insertBatch = owner.prepareStatement("""
                    INSERT INTO plant_batches (id, tenant_id, species, propagation_date, quantity_initial, quantity_current)
                    VALUES (?, ?, 'Ficus', CURRENT_DATE, 5, 5)
                    """)) {
                insertBatch.setObject(1, batchA);
                insertBatch.setObject(2, tenantA);
                insertBatch.executeUpdate();
            }
            try (PreparedStatement insertEvent = owner.prepareStatement("""
                    INSERT INTO batch_events (id, tenant_id, batch_id, event_type, occurred_at)
                    SELECT ?, ?, id, 'CREATED', now() FROM plant_batches WHERE tenant_id = ?
                    """)) {
                insertEvent.setObject(1, eventId);
                insertEvent.setObject(2, tenantA);
                insertEvent.setObject(3, tenantA);
                insertEvent.executeUpdate();
            }
            owner.commit();

            setTenant(owner, tenantB);
            try (PreparedStatement insertZone = owner.prepareStatement(
                    "INSERT INTO greenhouse_zones (id, tenant_id, name) VALUES (?, ?, ?)")) {
                insertZone.setObject(1, zoneB);
                insertZone.setObject(2, tenantB);
                insertZone.setString(3, "B zone");
                insertZone.executeUpdate();
            }
            owner.commit();

            try (Statement statement = owner.createStatement()) {
                statement.execute("CREATE ROLE " + APP_ROLE + " LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD '" + APP_PASSWORD + "'");
                statement.execute("GRANT CONNECT ON DATABASE nursery_crm TO " + APP_ROLE);
                statement.execute("GRANT USAGE ON SCHEMA public TO " + APP_ROLE);
                statement.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO " + APP_ROLE);
                statement.execute("GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO " + APP_ROLE);
            }
            owner.commit();
        }
    }

    @Test
    void forcedRlsScopesCrudAndTenantDefaultToTheTransactionLocalContext() throws SQLException {
        try (Connection app = appConnection()) {
            app.setAutoCommit(false);
            setTenant(app, tenantA);

            assertThat(countZones(app)).isGreaterThanOrEqualTo(1);
            assertThat(updateZone(app, zoneB)).isZero();
            assertThat(deleteZone(app, zoneB)).isZero();

            UUID ownedZone = UUID.randomUUID();
            try (var insert = app.prepareStatement("INSERT INTO greenhouse_zones (id, name) VALUES (?, ?)")) {
                insert.setObject(1, ownedZone);
                insert.setString(2, "Context-owned zone");
                assertThat(insert.executeUpdate()).isEqualTo(1);
            }
            assertThat(zoneTenant(app, ownedZone)).isEqualTo(tenantA);
            assertThat(zoneMicroclimate(app, ownedZone)).isEqualTo("{}");

            assertThatThrownBy(() -> insertForOtherTenant(app, UUID.randomUUID()))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("row-level security");
            app.rollback();

            app.setAutoCommit(false);
            setTenant(app, tenantA);
            assertThatThrownBy(() -> mutateEvent(app, "UPDATE"))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("append-only");
            app.rollback();

            app.setAutoCommit(false);
            setTenant(app, tenantA);
            assertThatThrownBy(() -> mutateEvent(app, "DELETE"))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("append-only");
            app.rollback();

            app.setAutoCommit(false);
            assertThat(countZones(app)).isZero();
            app.commit();
        }
    }

    @Test
    void hibernatePersistsStructuredJsonbWithTheNonOwnerApplicationRole() throws Exception {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
            .applySetting("hibernate.connection.driver_class", "org.postgresql.Driver")
            .applySetting("hibernate.connection.url", POSTGRES.getJdbcUrl())
            .applySetting("hibernate.connection.username", APP_ROLE)
            .applySetting("hibernate.connection.password", APP_PASSWORD)
            .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
            .applySetting("hibernate.hbm2ddl.auto", "none")
            .build();

        try (SessionFactory sessionFactory = new MetadataSources(registry)
                .addAnnotatedClass(GreenhouseZone.class)
                .addAnnotatedClass(PlantBatch.class)
                .addAnnotatedClass(BatchEvent.class)
                .buildMetadata()
                .buildSessionFactory()) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode targets = objectMapper.readTree("{\"temp_min\":20,\"temp_max\":24}");
            UUID zoneId;

            TenantContext.set(tenantA.toString());
            try (Session session = sessionFactory.openSession()) {
                var transaction = session.beginTransaction();
                session.doWork(connection -> setTenant(connection, tenantA));
                GreenhouseZone zone = new GreenhouseZone();
                zone.setName("Hibernate JSONB zone");
                zone.setMicroclimate(targets);
                session.persist(zone);
                transaction.commit();
                zoneId = zone.getId();
            }

            try (Session session = sessionFactory.openSession()) {
                var transaction = session.beginTransaction();
                session.doWork(connection -> setTenant(connection, tenantA));
                assertThat(session.find(GreenhouseZone.class, zoneId).getMicroclimate()).isEqualTo(targets);
                transaction.commit();
            }

            TenantContext.set(tenantA.toString());
            JsonNode payload = objectMapper.readTree("{\"quantity\":2}");
            UUID eventWithPayloadId;
            try (Session session = sessionFactory.openSession()) {
                var transaction = session.beginTransaction();
                session.doWork(connection -> setTenant(connection, tenantA));
                BatchEvent event = event(batchA, "MORTALITY_RECORDED");
                event.setPayload(payload);
                session.persist(event);
                transaction.commit();
                eventWithPayloadId = event.getId();
            }

            try (Session session = sessionFactory.openSession()) {
                var transaction = session.beginTransaction();
                session.doWork(connection -> setTenant(connection, tenantA));
                assertThat(session.find(BatchEvent.class, eventWithPayloadId).getPayload()).isEqualTo(payload);
                BatchEvent eventWithoutPayload = event(batchA, "BATCH_MOVED");
                session.persist(eventWithoutPayload);
                transaction.commit();
                assertThat(eventWithoutPayload.getPayload()).isEqualTo(objectMapper.createObjectNode());
            }
        } finally {
            TenantContext.clear();
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private static BatchEvent event(UUID batchId, String eventType) {
        BatchEvent event = new BatchEvent();
        event.setBatchId(batchId);
        event.setEventType(eventType);
        event.setOccurredAt(java.time.Instant.now());
        return event;
    }

    private static void setTenant(Connection connection, UUID tenantId) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT set_config('app.tenant_id', ?, true)")) {
            statement.setString(1, tenantId.toString());
            statement.execute();
        }
    }

    private static int countZones(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT count(*) FROM greenhouse_zones")) {
            rows.next();
            return rows.getInt(1);
        }
    }

    private static int updateZone(Connection connection, UUID id) throws SQLException {
        try (var statement = connection.prepareStatement("UPDATE greenhouse_zones SET name = 'changed' WHERE id = ?")) {
            statement.setObject(1, id);
            return statement.executeUpdate();
        }
    }

    private static int deleteZone(Connection connection, UUID id) throws SQLException {
        try (var statement = connection.prepareStatement("DELETE FROM greenhouse_zones WHERE id = ?")) {
            statement.setObject(1, id);
            return statement.executeUpdate();
        }
    }

    private static UUID zoneTenant(Connection connection, UUID id) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT tenant_id FROM greenhouse_zones WHERE id = ?")) {
            statement.setObject(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getObject(1, UUID.class);
            }
        }
    }

    private static String zoneMicroclimate(Connection connection, UUID id) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT microclimate::text FROM greenhouse_zones WHERE id = ?")) {
            statement.setObject(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getString(1);
            }
        }
    }

    private static void insertForOtherTenant(Connection connection, UUID id) throws SQLException {
        try (var statement = connection.prepareStatement("INSERT INTO greenhouse_zones (id, tenant_id, name) VALUES (?, ?, ?)")) {
            statement.setObject(1, id);
            statement.setObject(2, tenantB);
            statement.setString(3, "Spoofed tenant");
            statement.executeUpdate();
        }
    }

    private static void mutateEvent(Connection connection, String operation) throws SQLException {
        String sql = operation.equals("UPDATE")
            ? "UPDATE batch_events SET event_type = 'CHANGED' WHERE id = '" + eventId + "'"
            : "DELETE FROM batch_events WHERE id = '" + eventId + "'";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private static Connection ownerConnection() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private static Connection appConnection() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), APP_ROLE, APP_PASSWORD);
    }
}
