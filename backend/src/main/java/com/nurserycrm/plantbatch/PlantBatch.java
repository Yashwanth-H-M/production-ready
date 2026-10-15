package com.nurserycrm.plantbatch;

import com.nurserycrm.tenant.TenantOwned;
import com.nurserycrm.tenant.TenantOwnershipListener;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@EntityListeners(TenantOwnershipListener.class)
@Table(name = "plant_batches")
public class PlantBatch implements TenantOwned {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String species;

    @Column(name = "zone_id")
    private UUID zoneId;

    @Column(name = "propagation_date", nullable = false)
    private LocalDate propagationDate;

    @Column(name = "quantity_initial", nullable = false)
    private Integer quantityInitial;

    @Column(name = "quantity_current", nullable = false)
    private Integer quantityCurrent;

    @Column(name = "unit_cost", nullable = false)
    private java.math.BigDecimal unitCost;

    @Column(nullable = false)
    private String status = "propagating";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public UUID getZoneId() { return zoneId; }
    public void setZoneId(UUID zoneId) { this.zoneId = zoneId; }

    public LocalDate getPropagationDate() { return propagationDate; }
    public void setPropagationDate(LocalDate propagationDate) { this.propagationDate = propagationDate; }

    public Integer getQuantityInitial() { return quantityInitial; }
    public void setQuantityInitial(Integer quantityInitial) { this.quantityInitial = quantityInitial; }

    public Integer getQuantityCurrent() { return quantityCurrent; }
    public void setQuantityCurrent(Integer quantityCurrent) { this.quantityCurrent = quantityCurrent; }

    public java.math.BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(java.math.BigDecimal unitCost) { this.unitCost = unitCost; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
