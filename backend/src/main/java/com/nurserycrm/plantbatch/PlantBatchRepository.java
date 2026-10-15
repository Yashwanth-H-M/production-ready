package com.nurserycrm.plantbatch;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PlantBatchRepository extends JpaRepository<PlantBatch, UUID> {
}
