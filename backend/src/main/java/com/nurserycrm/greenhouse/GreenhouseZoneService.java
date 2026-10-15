package com.nurserycrm.greenhouse;

import com.nurserycrm.tenant.TenantAwareExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GreenhouseZoneService {

    private final GreenhouseZoneRepository repository;
    private final TenantAwareExecutor executor;

    public GreenhouseZoneService(GreenhouseZoneRepository repository, TenantAwareExecutor executor) {
        this.repository = repository;
        this.executor = executor;
    }

    public GreenhouseZone create(GreenhouseZone zone) {
        return executor.execute(() -> repository.save(zone));
    }

    public List<GreenhouseZone> findAll() {
        return executor.execute(() -> repository.findAll());
    }

    public GreenhouseZone findById(UUID id) {
        return executor.execute(() -> repository.findById(id).orElse(null));
    }

    public void delete(UUID id) {
        executor.execute(() -> { repository.deleteById(id); return null; });
    }
}
