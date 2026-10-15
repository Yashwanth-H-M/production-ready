package com.nurserycrm.greenhouse;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface GreenhouseZoneRepository extends JpaRepository<GreenhouseZone, UUID> {
}
