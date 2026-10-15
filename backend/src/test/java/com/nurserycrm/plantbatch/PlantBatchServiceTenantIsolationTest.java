package com.nurserycrm.plantbatch;

import com.nurserycrm.tenant.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;INIT=CREATE DOMAIN IF NOT EXISTS JSONB AS JSON",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "app.tenant-context-sql-enabled=false"
})
class PlantBatchServiceTenantIsolationTest {

    private static final String TENANT_A = "00000000-0000-0000-0000-000000000001";
    private static final String TENANT_B = "00000000-0000-0000-0000-000000000002";

    @Autowired
    private PlantBatchService service;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @Test
    void persistenceAssignsTheTenantFromTrustedContext() {
        TenantContext.set(TENANT_A);
        PlantBatch batchA = service.create(batch("Ficus", 100, 95, "propagating"));
        assertEquals(UUID.fromString(TENANT_A), batchA.getTenantId());

        TenantContext.set(TENANT_B);
        PlantBatch batchB = service.create(batch("Monstera", 50, 48, "growing"));
        assertEquals(UUID.fromString(TENANT_B), batchB.getTenantId());
    }

    @Test
    void executorRejectsMissingTenant() {
        TenantContext.clear();
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.create(batch("Ficus", 10, 10, "propagating")));
        assertTrue(ex.getMessage().contains("No tenant in context"));
    }

    private PlantBatch batch(String species, int initialQuantity, int currentQuantity, String status) {
        PlantBatch batch = new PlantBatch();
        batch.setSpecies(species);
        batch.setPropagationDate(LocalDate.now());
        batch.setQuantityInitial(initialQuantity);
        batch.setQuantityCurrent(currentQuantity);
        batch.setUnitCost(new BigDecimal("2.50"));
        batch.setStatus(status);
        return batch;
    }
}
