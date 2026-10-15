package com.nurserycrm.treatment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TreatmentRepository extends JpaRepository<Treatment, UUID> {
}
