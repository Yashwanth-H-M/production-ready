package com.nurserycrm.diagnostic;

import com.nurserycrm.tenant.TenantOwned;
import com.nurserycrm.tenant.TenantOwnershipListener;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@EntityListeners(TenantOwnershipListener.class)
@Table(name = "diagnostic_logs")
public class DiagnosticLog implements TenantOwned {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "symptom_notes")
    private String symptomNotes;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "cv_prediction")
    private String cvPrediction;

    @Column(name = "cv_confidence", precision = 4, scale = 3)
    private BigDecimal cvConfidence;

    @Column(name = "needs_review", nullable = false)
    private Boolean needsReview = true;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }

    public String getSymptomNotes() { return symptomNotes; }
    public void setSymptomNotes(String symptomNotes) { this.symptomNotes = symptomNotes; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getCvPrediction() { return cvPrediction; }
    public void setCvPrediction(String cvPrediction) { this.cvPrediction = cvPrediction; }

    public BigDecimal getCvConfidence() { return cvConfidence; }
    public void setCvConfidence(BigDecimal cvConfidence) { this.cvConfidence = cvConfidence; }

    public Boolean getNeedsReview() { return needsReview; }
    public void setNeedsReview(Boolean needsReview) { this.needsReview = needsReview; }

    public UUID getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UUID reviewedBy) { this.reviewedBy = reviewedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
