package com.nurserycrm.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs every incoming HTTP request and response status/duration,
 * suppressing noisy automated health probes.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HttpLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if ("/health".equals(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.currentTimeMillis();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");
        boolean hasBearer = authHeader != null && authHeader.startsWith("Bearer ");
        String clientIp = request.getRemoteAddr();

        log.info("--> [HTTP IN] {} {} (clientIp={}, hasBearer={})", method, uri, clientIp, hasBearer);

        try {
            filterChain.doFilter(request, response);
        } catch (Throwable t) {
            log.error("<-- [HTTP ERR] {} {} failed with {}: {}", method, uri, t.getClass().getSimpleName(), t.getMessage());
            throw t;
        } finally {
            long duration = System.currentTimeMillis() - start;
            int status = response.getStatus();
            log.info("<-- [HTTP OUT] {} {} -> status={} ({}ms)", method, uri, status, duration);
        }
    }
}
