package com.nurserycrm.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Converts only signature-verified Clerk claims into internal tenant identities. */
@Component
public class ClerkJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private static final UUID ID_NAMESPACE = UUID.fromString("f26a7612-947a-4d46-aed5-2c821f32f909");

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String subject = required(jwt, "sub");
        String organizationId = requiredOrganizationClaim(jwt, "org_id", "id");
        String organizationRole = requiredOrganizationClaim(jwt, "org_role", "rol");
        String email = required(jwt, "email");
        String organizationName = required(jwt, "org_name");

        // Clerk session token v1 uses org_role (for example, org:admin). V2
        // stores the active membership under `o` and abbreviates its role as
        // `rol` (for example, admin). In either format a signed, non-empty
        // active-organization role proves membership; authorization policy is
        // tenant-wide today and does not depend on the role's spelling.
        if (organizationRole.isBlank()) throw new BadCredentialsException("A Clerk organization membership is required.");

        String issuer = jwt.getIssuer().toString();
        UUID tenantId = stableId("canopy:clerk:tenant:" + issuer + ":" + organizationId);
        UUID userId = stableId("canopy:clerk:user:" + issuer + ":" + organizationId + ":" + subject);
        TenantPrincipal principal = new TenantPrincipal(userId, tenantId, email, organizationName, organizationId);
        return new UsernamePasswordAuthenticationToken(principal, jwt, List.of());
    }

    private static String required(Jwt jwt, String claim) {
        String value = jwt.getClaimAsString(claim);
        if (value == null || value.isBlank()) {
            throw new BadCredentialsException("The Clerk session is missing a required " + claim + " claim.");
        }
        return value;
    }

    private static String requiredOrganizationClaim(Jwt jwt, String legacyClaim, String v2Claim) {
        String legacyValue = jwt.getClaimAsString(legacyClaim);
        if (legacyValue != null && !legacyValue.isBlank()) return legacyValue;

        Object organizationClaim = jwt.getClaim("o");
        if (organizationClaim instanceof Map<?, ?> organization) {
            Object value = organization.get(v2Claim);
            if (value instanceof String stringValue && !stringValue.isBlank()) return stringValue;
        }
        throw new BadCredentialsException("The Clerk session is missing a required "
            + legacyClaim + " or o." + v2Claim + " claim.");
    }

    private static UUID stableId(String value) {
        ByteBuffer namespace = ByteBuffer.allocate(16)
            .putLong(ID_NAMESPACE.getMostSignificantBits())
            .putLong(ID_NAMESPACE.getLeastSignificantBits());
        byte[] name = value.getBytes(StandardCharsets.UTF_8);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1").digest(
                ByteBuffer.allocate(namespace.capacity() + name.length)
                    .put(namespace.array())
                    .put(name)
                    .array());
            digest[6] = (byte) ((digest[6] & 0x0f) | 0x50);
            digest[8] = (byte) ((digest[8] & 0x3f) | 0x80);
            ByteBuffer uuid = ByteBuffer.wrap(digest);
            return new UUID(uuid.getLong(), uuid.getLong());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-1 is required by the UUID v5 standard.", exception);
        }
    }
}
