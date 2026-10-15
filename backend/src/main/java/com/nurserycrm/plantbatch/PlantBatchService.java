package com.nurserycrm.plantbatch;

import com.nurserycrm.greenhouse.GreenhouseZoneRepository;
import com.nurserycrm.tenant.TenantAwareExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PlantBatchService {

    private final PlantBatchRepository repository;
    private final GreenhouseZoneRepository greenhouseZoneRepository;
    private final TenantAwareExecutor executor;

    public PlantBatchService(PlantBatchRepository repository,
                             GreenhouseZoneRepository greenhouseZoneRepository,
                             TenantAwareExecutor executor) {
        this.repository = repository;
        this.greenhouseZoneRepository = greenhouseZoneRepository;
        this.executor = executor;
    }

    public PlantBatch create(PlantBatch batch) {
        return executor.execute(() -> {
            validateBatch(batch);
            return repository.save(batch);
        });
    }

    public List<PlantBatch> findAll() {
        return executor.execute(repository::findAll);
    }

    public PlantBatch findById(UUID id) {
        return executor.execute(() -> repository.findById(id).orElse(null));
    }

    public PlantBatch update(UUID id, PlantBatch updated) {
        return executor.execute(() -> {
            validateBatch(updated);
            PlantBatch existing = repository.findById(id).orElse(null);
            if (existing == null) {
                return null;
            }
            existing.setSpecies(updated.getSpecies());
            existing.setZoneId(updated.getZoneId());
            existing.setPropagationDate(updated.getPropagationDate());
            existing.setQuantityInitial(updated.getQuantityInitial());
            existing.setQuantityCurrent(updated.getQuantityCurrent());
            existing.setUnitCost(updated.getUnitCost());
            existing.setStatus(updated.getStatus());
            return repository.save(existing);
        });
    }

    public void delete(UUID id) {
        executor.execute(() -> { repository.deleteById(id); return null; });
    }

    private void validateBatch(PlantBatch batch) {
        if (batch.getQuantityInitial() == null || batch.getQuantityInitial() < 0) {
            throw new IllegalArgumentException("Initial quantity must be zero or greater.");
        }
        if (batch.getQuantityCurrent() == null || batch.getQuantityCurrent() < 0) {
            throw new IllegalArgumentException("Current quantity must be zero or greater.");
        }
        if (batch.getQuantityCurrent() > batch.getQuantityInitial()) {
            throw new IllegalArgumentException("Current quantity cannot exceed initial quantity.");
        }
        if (batch.getUnitCost() == null || batch.getUnitCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit cost must be zero or greater.");
        }
        if (batch.getZoneId() != null && !greenhouseZoneRepository.existsById(batch.getZoneId())) {
            throw new IllegalArgumentException("The selected greenhouse zone is unavailable in this workspace.");
        }
    }
}
