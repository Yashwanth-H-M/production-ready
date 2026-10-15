package com.nurserycrm.treatment;

import com.nurserycrm.plantbatch.PlantBatchRepository;
import com.nurserycrm.tenant.TenantAwareExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TreatmentService {

    private final TreatmentRepository repository;
    private final PlantBatchRepository plantBatchRepository;
    private final TenantAwareExecutor executor;

    public TreatmentService(TreatmentRepository repository,
                            PlantBatchRepository plantBatchRepository,
                            TenantAwareExecutor executor) {
        this.repository = repository;
        this.plantBatchRepository = plantBatchRepository;
        this.executor = executor;
    }

    public Treatment create(Treatment treatment) {
        return executor.execute(() -> {
            if (!plantBatchRepository.existsById(treatment.getBatchId())) {
                throw new IllegalArgumentException("The selected plant batch is unavailable in this workspace.");
            }
            return repository.save(treatment);
        });
    }

    public List<Treatment> findAll() {
        return executor.execute(repository::findAll);
    }

    public Treatment findById(UUID id) {
        return executor.execute(() -> repository.findById(id).orElse(null));
    }

    public void delete(UUID id) {
        executor.execute(() -> { repository.deleteById(id); return null; });
    }
}
