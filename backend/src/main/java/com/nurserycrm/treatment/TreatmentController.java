package com.nurserycrm.treatment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/treatments")
public class TreatmentController {

    private final TreatmentService service;

    public TreatmentController(TreatmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Treatment> create(@Valid @RequestBody TreatmentRequest request) {
        Treatment treatment = new Treatment();
        treatment.setBatchId(request.batchId());
        treatment.setProductName(request.productName());
        treatment.setTreatmentType(request.treatmentType());
        treatment.setAppliedDate(request.appliedDate());
        treatment.setApplicationRate(request.applicationRate());
        treatment.setWithholdingDays(request.withholdingDays());
        return ResponseEntity.ok(service.create(treatment));
    }

    @GetMapping
    public ResponseEntity<List<Treatment>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Treatment> get(@PathVariable UUID id) {
        Treatment treatment = service.findById(id);
        return treatment != null ? ResponseEntity.ok(treatment) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record TreatmentRequest(
        @NotNull UUID batchId,
        @NotBlank String productName,
        @NotBlank String treatmentType,
        @NotNull LocalDate appliedDate,
        String applicationRate,
        @NotNull @PositiveOrZero Integer withholdingDays
    ) {}
}
