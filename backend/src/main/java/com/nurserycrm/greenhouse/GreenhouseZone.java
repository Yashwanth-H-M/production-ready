package com.nurserycrm.greenhouse;

import com.nurserycrm.tenant.TenantOwned;
import com.nurserycrm.tenant.TenantOwnershipListener;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

@Entity
@EntityListeners(TenantOwnershipListener.class)
@Table(name = "greenhouse_zones")
public class GreenhouseZone implements TenantOwned {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode microclimate = JsonNodeFactory.instance.objectNode();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public JsonNode getMicroclimate() { return microclimate; }
    public void setMicroclimate(JsonNode microclimate) {
        this.microclimate = microclimate == null ? JsonNodeFactory.instance.objectNode() : microclimate;
    }
}
