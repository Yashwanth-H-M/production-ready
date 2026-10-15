package com.nurserycrm.tenant;

import java.util.UUID;

/** Marks a persistence entity whose tenant must be assigned by trusted server context. */
public interface TenantOwned {
    UUID getTenantId();
    void setTenantId(UUID tenantId);
}
