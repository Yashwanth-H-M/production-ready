package com.nurserycrm.tenant;

import com.nurserycrm.security.TenantPrincipal;
import com.nurserycrm.identity.TenantProvisioningService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Sets the transaction tenant from a signature-verified principal. The legacy
 * X-Tenant-ID header is never trusted and mismatches are rejected explicitly.
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    private final TenantProvisioningService tenantProvisioningService;

    public TenantFilter(TenantProvisioningService tenantProvisioningService) {
        this.tenantProvisioningService = tenantProvisioningService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof TenantPrincipal principal) {
                String suppliedTenant = request.getHeader("X-Tenant-ID");
                if (suppliedTenant != null && !suppliedTenant.isBlank()
                    && !suppliedTenant.equals(principal.tenantId().toString())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant selection does not match the authenticated workspace.");
                    return;
                }
                TenantContext.set(principal.tenantId().toString());
                tenantProvisioningService.ensureIdentity(principal);
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
