package com.nurserycrm.identity;

import com.nurserycrm.security.TenantPrincipal;
import com.nurserycrm.tenant.TenantAwareExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TenantProvisioningServiceTest {

    private JdbcTemplate jdbcTemplate;
    private IdentityUserRepository users;
    private TenantAwareExecutor tenantAwareExecutor;
    private TenantProvisioningService service;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        users = mock(IdentityUserRepository.class);
        tenantAwareExecutor = mock(TenantAwareExecutor.class);
        service = new TenantProvisioningService(jdbcTemplate, users, tenantAwareExecutor);
    }

    @Test
    void ignoresPrincipalWithoutClerkOrganization() {
        TenantPrincipal principal = new TenantPrincipal(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "grower@greenvalley.com",
            "Green Valley",
            null
        );

        service.ensureIdentity(principal);

        verify(jdbcTemplate, never()).update(any(String.class), any(), any());
        verify(tenantAwareExecutor, never()).execute(any());
    }

    @Test
    void provisionsTenantAndUserWithAtomicUpserts() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        TenantPrincipal principal = new TenantPrincipal(
            userId,
            tenantId,
            "grower@greenvalley.com",
            "Green Valley Nursery",
            "org_123"
        );

        when(tenantAwareExecutor.execute(any())).thenAnswer(invocation -> {
            Supplier<?> work = invocation.getArgument(0);
            return work.get();
        });

        service.ensureIdentity(principal);

        verify(jdbcTemplate).update(
            contains("INSERT INTO tenants"),
            eq(tenantId),
            eq("Green Valley Nursery")
        );

        verify(tenantAwareExecutor).execute(any());

        verify(jdbcTemplate).update(
            contains("INSERT INTO users"),
            eq(userId),
            eq(tenantId),
            eq("grower@greenvalley.com")
        );
    }
}
