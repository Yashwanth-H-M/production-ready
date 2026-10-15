package com.nurserycrm.identity;

import com.nurserycrm.security.TenantPrincipal;
import com.nurserycrm.tenant.TenantAwareExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Creates the internal tenant and user records from signed Clerk organization claims. */
@Service
public class TenantProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(TenantProvisioningService.class);
    private final JdbcTemplate jdbcTemplate;
    private final IdentityUserRepository users;
    private final TenantAwareExecutor tenantAwareExecutor;

    public TenantProvisioningService(JdbcTemplate jdbcTemplate,
                                     IdentityUserRepository users,
                                     TenantAwareExecutor tenantAwareExecutor) {
        this.jdbcTemplate = jdbcTemplate;
        this.users = users;
        this.tenantAwareExecutor = tenantAwareExecutor;
    }

    public void ensureIdentity(TenantPrincipal principal) {
        if (principal.clerkOrganizationId() == null) {
            log.debug("Skipping provisioning: principal has no Clerk organization ID");
            return;
        }

        log.debug("Ensuring tenant row: id={}, name={}", principal.tenantId(), principal.tenantName());
        // tenants is the global organization directory, not tenant-owned data.
        jdbcTemplate.update("""
            INSERT INTO tenants (id, name) VALUES (?, ?)
            ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name
            WHERE tenants.name IS DISTINCT FROM EXCLUDED.name
            """, principal.tenantId(), principal.tenantName());

        log.debug("Ensuring user row in tenant context: userId={}, email={}", principal.userId(), principal.email());
        tenantAwareExecutor.execute(() -> {
            jdbcTemplate.update("""
                INSERT INTO users (id, tenant_id, email, role)
                VALUES (?, ?, ?, 'grower')
                ON CONFLICT (id) DO UPDATE SET email = EXCLUDED.email
                WHERE users.email IS DISTINCT FROM EXCLUDED.email
                """, principal.userId(), principal.tenantId(), principal.email());
            return null;
        });
        log.info("Identity verified and synchronized for user={} in tenant={}", principal.userId(), principal.tenantId());
    }
}
