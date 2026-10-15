package com.nurserycrm.diagnostic;

import com.nurserycrm.security.TenantPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/diagnostics")
public class DiagnosticLogController {

    private final DiagnosticLogService service;

    public DiagnosticLogController(DiagnosticLogService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DiagnosticLog> create(@Valid @RequestBody DiagnosticRequest request) {
        DiagnosticLog log = new DiagnosticLog();
        log.setBatchId(request.batchId());
        log.setSymptomNotes(request.symptomNotes());
        log.setPhotoUrl(request.photoUrl());
        // Diagnostic predictions are accepted only as advisory data. Human review
        // remains mandatory regardless of confidence.
        log.setCvPrediction(request.cvPrediction());
        log.setCvConfidence(request.cvConfidence());
        log.setNeedsReview(true);
        return ResponseEntity.ok(service.create(log));
    }

    @GetMapping
    public ResponseEntity<List<DiagnosticLog>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiagnosticLog> get(@PathVariable UUID id) {
        DiagnosticLog log = service.findById(id);
        return log != null ? ResponseEntity.ok(log) : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<DiagnosticLog> review(@PathVariable UUID id, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof TenantPrincipal principal)) {
            return ResponseEntity.status(401).build();
        }
        DiagnosticLog reviewed = service.markReviewed(id, principal.userId());
        return reviewed != null ? ResponseEntity.ok(reviewed) : ResponseEntity.notFound().build();
    }

    public record DiagnosticRequest(
        @NotNull UUID batchId,
        String symptomNotes,
        String photoUrl,
        String cvPrediction,
        BigDecimal cvConfidence
    ) {}
}
