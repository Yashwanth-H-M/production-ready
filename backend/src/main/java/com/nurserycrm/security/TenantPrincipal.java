package com.nurserycrm.security;

import java.util.UUID;

/**
 * Identity extracted from a signature-verified access token. Controllers and
 * tenant filters must use this instead of caller-provided request headers.
 */
public record TenantPrincipal(UUID userId, UUID tenantId, String email,
                              String tenantName, String clerkOrganizationId) {
    public TenantPrincipal(UUID userId, UUID tenantId, String email) {
        this(userId, tenantId, email, null, null);
    }
}
