package com.nurserycrm.tenant;

import jakarta.persistence.PrePersist;

import java.util.UUID;

/** Prevents API request bodies from setting tenant ownership directly. */
public class TenantOwnershipListener {

    @PrePersist
    public void assignTenant(Object target) {
        if (!(target instanceof TenantOwned entity)) {
            throw new IllegalStateException("Tenant ownership listener attached to an unsupported entity.");
        }
        String tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant in context — refusing to persist tenant-scoped data");
        }
        // Caller-supplied ownership must never override the authenticated tenant.
        entity.setTenantId(UUID.fromString(tenantId));
    }
}
