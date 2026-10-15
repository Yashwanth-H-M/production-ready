package com.nurserycrm.treatment;

import com.nurserycrm.tenant.TenantOwned;
import com.nurserycrm.tenant.TenantOwnershipListener;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@EntityListeners(TenantOwnershipListener.class)
@Table(name = "treatments")
public class Treatment implements TenantOwned {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "treatment_type", nullable = false)
    private String treatmentType;

    @Column(name = "applied_date", nullable = false)
    private LocalDate appliedDate;

    @Column(name = "application_rate")
    private String applicationRate;

    @Column(name = "withholding_days", nullable = false)
    private Integer withholdingDays;

    @Column(name = "safe_ship_date", insertable = false, updatable = false)
    private LocalDate safeShipDate;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getTreatmentType() { return treatmentType; }
    public void setTreatmentType(String treatmentType) { this.treatmentType = treatmentType; }

    public LocalDate getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; }

    public String getApplicationRate() { return applicationRate; }
    public void setApplicationRate(String applicationRate) { this.applicationRate = applicationRate; }

    public Integer getWithholdingDays() { return withholdingDays; }
    public void setWithholdingDays(Integer withholdingDays) { this.withholdingDays = withholdingDays; }

    public LocalDate getSafeShipDate() { return safeShipDate; }
}
