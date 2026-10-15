package com.nurserycrm.plantbatch;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plant-batches")
public class PlantBatchController {

    private final PlantBatchService service;

    public PlantBatchController(PlantBatchService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PlantBatch> create(@Valid @RequestBody PlantBatchRequest request) {
        PlantBatch batch = new PlantBatch();
        batch.setSpecies(request.species());
        batch.setZoneId(request.zoneId());
        batch.setPropagationDate(request.propagationDate());
        batch.setQuantityInitial(request.quantityInitial());
        batch.setQuantityCurrent(request.quantityCurrent());
        batch.setUnitCost(request.unitCost());
        batch.setStatus(request.status() != null ? request.status() : "propagating");
        return ResponseEntity.ok(service.create(batch));
    }

    @GetMapping
    public ResponseEntity<List<PlantBatch>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantBatch> get(@PathVariable UUID id) {
        PlantBatch batch = service.findById(id);
        return batch != null ? ResponseEntity.ok(batch) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlantBatch> update(@PathVariable UUID id, @Valid @RequestBody PlantBatchRequest request) {
        PlantBatch batch = new PlantBatch();
        batch.setSpecies(request.species());
        batch.setZoneId(request.zoneId());
        batch.setPropagationDate(request.propagationDate());
        batch.setQuantityInitial(request.quantityInitial());
        batch.setQuantityCurrent(request.quantityCurrent());
        batch.setUnitCost(request.unitCost());
        batch.setStatus(request.status());
        return ResponseEntity.ok(service.update(id, batch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record PlantBatchRequest(
        @NotBlank String species,
        UUID zoneId,
        @NotNull LocalDate propagationDate,
        @NotNull @Positive Integer quantityInitial,
        @NotNull @PositiveOrZero Integer quantityCurrent,
        @NotNull BigDecimal unitCost,
        String status
    ) {}
}
