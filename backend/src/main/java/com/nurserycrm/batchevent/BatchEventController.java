package com.nurserycrm.batchevent;

import com.fasterxml.jackson.databind.JsonNode;
import com.nurserycrm.security.TenantPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/batch-events")
public class BatchEventController {

    private final BatchEventService service;

    public BatchEventController(BatchEventService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BatchEvent> create(@Valid @RequestBody BatchEventRequest request,
                                              Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof TenantPrincipal principal)) {
            return ResponseEntity.status(401).build();
        }
        BatchEvent event = new BatchEvent();
        event.setBatchId(request.batchId());
        event.setEventType(request.eventType());
        event.setPayload(request.payload());
        event.setOccurredAt(request.occurredAt() != null ? request.occurredAt() : Instant.now());
        event.setRecordedBy(principal.userId());
        return ResponseEntity.ok(service.create(event));
    }

    @GetMapping
    public ResponseEntity<List<BatchEvent>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<List<BatchEvent>> byBatch(@PathVariable UUID batchId) {
        return ResponseEntity.ok(service.findByBatchId(batchId));
    }

    public record BatchEventRequest(
        @NotNull UUID batchId,
        @NotBlank String eventType,
        JsonNode payload,
        Instant occurredAt
    ) {}
}
