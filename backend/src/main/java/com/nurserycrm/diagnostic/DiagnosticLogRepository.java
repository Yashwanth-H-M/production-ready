package com.nurserycrm.diagnostic;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DiagnosticLogRepository extends JpaRepository<DiagnosticLog, UUID> {
}
