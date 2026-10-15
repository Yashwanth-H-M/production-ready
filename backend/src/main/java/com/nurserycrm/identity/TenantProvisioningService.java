package com.nurserycrm.identity;

import com.nurserycrm.security.TenantPrincipal;
import com.nurserycrm.tenant.TenantAwareExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Creates the internal tenant and user records from signed Clerk organization claims. */
@Service
public class TenantProvisioningService {

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
            return;
        }

        // tenants is the global organization directory, not tenant-owned data.
        jdbcTemplate.update("""
            INSERT INTO tenants (id, name) VALUES (?, ?)
            ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name
            WHERE tenants.name IS DISTINCT FROM EXCLUDED.name
            """, principal.tenantId(), principal.tenantName());

        tenantAwareExecutor.execute(() -> {
            IdentityUser user = users.findById(principal.userId()).orElse(null);
            if (user == null) {
                users.save(new IdentityUser(principal.userId(), principal.email()));
            } else if (!user.getEmail().equals(principal.email())) {
                user.setEmail(principal.email());
                users.save(user);
            }
            return null;
        });
    }
}
