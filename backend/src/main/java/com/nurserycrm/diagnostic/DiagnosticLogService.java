package com.nurserycrm.diagnostic;

import com.nurserycrm.plantbatch.PlantBatchRepository;
import com.nurserycrm.tenant.TenantAwareExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DiagnosticLogService {

    private final DiagnosticLogRepository repository;
    private final PlantBatchRepository plantBatchRepository;
    private final TenantAwareExecutor executor;

    public DiagnosticLogService(DiagnosticLogRepository repository,
                                PlantBatchRepository plantBatchRepository,
                                TenantAwareExecutor executor) {
        this.repository = repository;
        this.plantBatchRepository = plantBatchRepository;
        this.executor = executor;
    }

    public DiagnosticLog create(DiagnosticLog log) {
        return executor.execute(() -> {
            if (!plantBatchRepository.existsById(log.getBatchId())) {
                throw new IllegalArgumentException("The selected plant batch is unavailable in this workspace.");
            }
            log.setNeedsReview(true);
            return repository.save(log);
        });
    }

    public List<DiagnosticLog> findAll() {
        return executor.execute(repository::findAll);
    }

    public DiagnosticLog findById(UUID id) {
        return executor.execute(() -> repository.findById(id).orElse(null));
    }

    public DiagnosticLog markReviewed(UUID id, UUID reviewedBy) {
        return executor.execute(() -> {
            DiagnosticLog log = repository.findById(id).orElse(null);
            if (log == null) {
                return null;
            }
            log.setNeedsReview(false);
            log.setReviewedBy(reviewedBy);
            return repository.save(log);
        });
    }
}
