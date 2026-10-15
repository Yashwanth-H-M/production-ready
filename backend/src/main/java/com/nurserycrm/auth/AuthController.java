package com.nurserycrm.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Local-development session bootstrap only. Production authentication must be
 * supplied by a real identity provider; this endpoint is disabled by default.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${app.local-demo-auth-enabled:false}")
    private boolean localDemoAuthEnabled;

    @Value("${app.local-demo-user-id}")
    private String localDemoUserId;

    @Value("${app.local-demo-tenant-id}")
    private String localDemoTenantId;

    @Value("${app.local-demo-email}")
    private String localDemoEmail;

    @PostMapping("/local-session")
    public TokenResponse localSession() {
        if (!localDemoAuthEnabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
            .subject(localDemoUserId)
            .claim("tenant_id", localDemoTenantId)
            .claim("email", localDemoEmail)
            .expiration(Date.from(Instant.now().plus(8, ChronoUnit.HOURS)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
        return new TokenResponse(token);
    }

    public record TokenResponse(String token) {}
}
