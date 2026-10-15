package com.nurserycrm.tenant;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

public class TenantAwareExecutor {
    private final TransactionTemplate transactionTemplate;
    private final EntityManager entityManager;
    private final boolean tenantContextSqlEnabled;

    public TenantAwareExecutor(TransactionTemplate transactionTemplate,
                               EntityManager entityManager,
                               boolean tenantContextSqlEnabled) {
        this.transactionTemplate = transactionTemplate;
        this.entityManager = entityManager;
        this.tenantContextSqlEnabled = tenantContextSqlEnabled;
    }

    public <T> T execute(Supplier<T> work) {
        return transactionTemplate.execute(status -> {
            String tenantId = TenantContext.get();
            if (tenantId == null) {
                throw new IllegalStateException(
                    "No tenant in context — refusing to run a tenant-scoped query");
            }
            if (tenantContextSqlEnabled) {
                entityManager.createNativeQuery(
                        "SELECT set_config('app.tenant_id', :tenantId, true)")
                    .setParameter("tenantId", tenantId)
                    .getSingleResult();
            }
            return work.get();
        });
    }
}
