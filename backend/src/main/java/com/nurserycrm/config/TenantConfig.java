package com.nurserycrm.config;

import com.nurserycrm.tenant.TenantAwareExecutor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class TenantConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public TenantAwareExecutor tenantAwareExecutor(
            TransactionTemplate transactionTemplate,
            @Value("${app.tenant-context-sql-enabled:true}") boolean tenantContextSqlEnabled) {
        return new TenantAwareExecutor(transactionTemplate, entityManager, tenantContextSqlEnabled);
    }
}
