package com.nurserycrm.greenhouse;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/greenhouse-zones")
public class GreenhouseZoneController {

    private final GreenhouseZoneService service;

    public GreenhouseZoneController(GreenhouseZoneService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<GreenhouseZone> create(@Valid @RequestBody ZoneRequest request) {
        GreenhouseZone zone = new GreenhouseZone();
        zone.setName(request.name());
        zone.setMicroclimate(request.microclimate());
        return ResponseEntity.ok(service.create(zone));
    }

    @GetMapping
    public ResponseEntity<List<GreenhouseZone>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GreenhouseZone> get(@PathVariable UUID id) {
        GreenhouseZone zone = service.findById(id);
        return zone != null ? ResponseEntity.ok(zone) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record ZoneRequest(@NotBlank String name, JsonNode microclimate) {}
}
