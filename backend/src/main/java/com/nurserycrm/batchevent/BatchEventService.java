package com.nurserycrm.batchevent;

import com.nurserycrm.plantbatch.PlantBatchRepository;
import com.nurserycrm.tenant.TenantAwareExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BatchEventService {

    private final BatchEventRepository repository;
    private final PlantBatchRepository plantBatchRepository;
    private final TenantAwareExecutor executor;

    public BatchEventService(BatchEventRepository repository,
                             PlantBatchRepository plantBatchRepository,
                             TenantAwareExecutor executor) {
        this.repository = repository;
        this.plantBatchRepository = plantBatchRepository;
        this.executor = executor;
    }

    public BatchEvent create(BatchEvent event) {
        return executor.execute(() -> {
            if (!plantBatchRepository.existsById(event.getBatchId())) {
                throw new IllegalArgumentException("The selected plant batch is unavailable in this workspace.");
            }
            return repository.save(event);
        });
    }

    public List<BatchEvent> findAll() {
        return executor.execute(repository::findAll);
    }

    public List<BatchEvent> findByBatchId(UUID batchId) {
        return executor.execute(() -> repository.findByBatchId(batchId));
    }
}
