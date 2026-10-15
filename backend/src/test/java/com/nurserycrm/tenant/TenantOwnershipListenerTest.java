package com.nurserycrm.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantOwnershipListenerTest {
    private final TenantOwnershipListener listener = new TenantOwnershipListener();

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void trustedTenantContextOverridesCallerSuppliedOwnership() {
        UUID trustedTenant = UUID.randomUUID();
        TenantOwned entity = new TestTenantEntity(UUID.randomUUID());
        TenantContext.set(trustedTenant.toString());

        listener.assignTenant(entity);

        assertThat(entity.getTenantId()).isEqualTo(trustedTenant);
    }

    @Test
    void refusesToPersistWithoutTrustedTenantContext() {
        TenantOwned entity = new TestTenantEntity(UUID.randomUUID());

        assertThatThrownBy(() -> listener.assignTenant(entity))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("No tenant in context");
    }

    private static final class TestTenantEntity implements TenantOwned {
        private UUID tenantId;

        private TestTenantEntity(UUID tenantId) {
            this.tenantId = tenantId;
        }

        @Override
        public UUID getTenantId() {
            return tenantId;
        }

        @Override
        public void setTenantId(UUID tenantId) {
            this.tenantId = tenantId;
        }
    }
}
