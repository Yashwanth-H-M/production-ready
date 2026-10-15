package com.nurserycrm.order;

import com.nurserycrm.tenant.TenantOwned;
import com.nurserycrm.tenant.TenantOwnershipListener;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@EntityListeners(TenantOwnershipListener.class)
@Table(name = "order_line_items")
public class OrderLineItem implements TenantOwned {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "batch_id")
    private UUID batchId;

    @Column(nullable = false)
    private String species;

    @Column(name = "promised_qty", nullable = false)
    private Integer promisedQty;

    @Column(name = "promised_date")
    private LocalDate promisedDate;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public Integer getPromisedQty() { return promisedQty; }
    public void setPromisedQty(Integer promisedQty) { this.promisedQty = promisedQty; }

    public LocalDate getPromisedDate() { return promisedDate; }
    public void setPromisedDate(LocalDate promisedDate) { this.promisedDate = promisedDate; }
}
