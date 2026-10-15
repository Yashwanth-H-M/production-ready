package com.nurserycrm.greenhouse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nurserycrm.batchevent.BatchEvent;
import com.nurserycrm.batchevent.BatchEventService;
import com.nurserycrm.plantbatch.PlantBatch;
import com.nurserycrm.plantbatch.PlantBatchService;
import com.nurserycrm.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;INIT=CREATE DOMAIN IF NOT EXISTS JSONB AS JSON",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "app.tenant-context-sql-enabled=false"
})
class JsonFieldPersistenceTest {

    private static final String TENANT_ID = "00000000-0000-0000-0000-000000000031";

    @Autowired
    private GreenhouseZoneService zoneService;

    @Autowired
    private PlantBatchService batchService;

    @Autowired
    private BatchEventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void persistsStructuredJsonAndDefaultsMissingProfilesToAnObject() throws Exception {
        TenantContext.set(TENANT_ID);

        GreenhouseZone zoneWithTargets = new GreenhouseZone();
        zoneWithTargets.setName("Propagation house");
        JsonNode targets = objectMapper.readTree("{\"temp_min\":20,\"temp_max\":24}");
        zoneWithTargets.setMicroclimate(targets);
        assertEquals(targets, zoneService.create(zoneWithTargets).getMicroclimate());

        GreenhouseZone zoneWithoutTargets = new GreenhouseZone();
        zoneWithoutTargets.setName("Holding house");
        assertEquals(objectMapper.createObjectNode(), zoneService.create(zoneWithoutTargets).getMicroclimate());
    }

    @Test
    void persistsStructuredEventPayloadAndDefaultsMissingPayloadToAnObject() throws Exception {
        TenantContext.set(TENANT_ID);
        PlantBatch batch = new PlantBatch();
        batch.setSpecies("Ficus");
        batch.setPropagationDate(LocalDate.now());
        batch.setQuantityInitial(10);
        batch.setQuantityCurrent(10);
        batch.setUnitCost(new BigDecimal("1.00"));
        batch = batchService.create(batch);

        BatchEvent eventWithPayload = new BatchEvent();
        eventWithPayload.setBatchId(batch.getId());
        eventWithPayload.setEventType("MORTALITY_RECORDED");
        eventWithPayload.setOccurredAt(Instant.now());
        JsonNode payload = objectMapper.readTree("{\"quantity\":2}");
        eventWithPayload.setPayload(payload);
        assertEquals(payload, eventService.create(eventWithPayload).getPayload());

        BatchEvent eventWithoutPayload = new BatchEvent();
        eventWithoutPayload.setBatchId(batch.getId());
        eventWithoutPayload.setEventType("BATCH_MOVED");
        eventWithoutPayload.setOccurredAt(Instant.now());
        assertEquals(objectMapper.createObjectNode(), eventService.create(eventWithoutPayload).getPayload());
    }
}
