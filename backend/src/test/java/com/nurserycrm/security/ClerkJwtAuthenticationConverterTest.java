package com.nurserycrm.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClerkJwtAuthenticationConverterTest {

    private final ClerkJwtAuthenticationConverter converter = new ClerkJwtAuthenticationConverter();

    @Test
    void derivesStableInternalIdentityFromVerifiedOrganizationClaims() {
        var first = convert(token("org_alpha", "user_1"));
        var same = convert(token("org_alpha", "user_1"));
        var otherOrganization = convert(token("org_beta", "user_1"));

        assertThat(first.tenantId()).isEqualTo(same.tenantId());
        assertThat(first.userId()).isEqualTo(same.userId());
        assertThat(first.tenantId()).isNotEqualTo(otherOrganization.tenantId());
        assertThat(first.userId()).isNotEqualTo(otherOrganization.userId());
        assertThat(first.email()).isEqualTo("grower@example.com");
        assertThat(first.tenantName()).isEqualTo("North greenhouse");
    }

    @Test
    void acceptsClerkSessionTokenV2OrganizationClaims() {
        Jwt v2Token = Jwt.withTokenValue("verified-token")
            .header("alg", "RS256")
            .issuer("https://canopy.clerk.accounts.dev")
            .subject("user_1")
            .claim("o", Map.of("id", "org_alpha", "rol", "admin"))
            .claim("email", "grower@example.com")
            .claim("org_name", "North greenhouse")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();

        var converted = convert(v2Token);
        assertThat(converted.tenantId()).isEqualTo(convert(token("org_alpha", "user_1")).tenantId());
        assertThat(converted.userId()).isEqualTo(convert(token("org_alpha", "user_1")).userId());
    }

    @Test
    void rejectsClerkSessionTokenV2WithoutOrganizationRole() {
        Jwt v2Token = Jwt.withTokenValue("verified-token")
            .header("alg", "RS256")
            .issuer("https://canopy.clerk.accounts.dev")
            .subject("user_1")
            .claim("o", Map.of("id", "org_alpha"))
            .claim("email", "grower@example.com")
            .claim("org_name", "North greenhouse")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();

        assertThatThrownBy(() -> converter.convert(v2Token))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessageContaining("org_role or o.rol");
    }

    @Test
    void rejectsTokensWithoutOrganizationMembership() {
        Jwt token = Jwt.withTokenValue("verified-token")
            .header("alg", "RS256")
            .issuer("https://canopy.clerk.accounts.dev")
            .subject("user_1")
            .claim("org_role", "org:member")
            .claim("email", "grower@example.com")
            .claim("org_name", "North greenhouse")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();

        assertThatThrownBy(() -> converter.convert(token))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessageContaining("org_id or o.id");
    }

    private TenantPrincipal convert(Jwt jwt) {
        var authentication = converter.convert(jwt);
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.isAuthenticated()).isTrue();
        return (TenantPrincipal) authentication.getPrincipal();
    }

    private Jwt token(String organizationId, String userId) {
        return Jwt.withTokenValue("verified-token")
            .header("alg", "RS256")
            .issuer("https://canopy.clerk.accounts.dev")
            .subject(userId)
            .claim("org_id", organizationId)
            .claim("org_role", "org:member")
            .claim("email", "grower@example.com")
            .claim("org_name", "North greenhouse")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
    }
}
