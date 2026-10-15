package com.nurserycrm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.util.Assert;

@Configuration
@ConditionalOnProperty(name = "app.auth.provider", havingValue = "clerk")
public class ClerkJwtConfiguration {

    @Bean
    JwtDecoder clerkJwtDecoder(@Value("${app.clerk.issuer}") String issuer,
                               @Value("${app.clerk.jwks-uri}") String jwksUri,
                               @Value("${app.clerk.audience}") String audience) {
        Assert.hasText(issuer, "CLERK_ISSUER is required when AUTH_PROVIDER=clerk");
        Assert.hasText(jwksUri, "CLERK_JWKS_URL is required when AUTH_PROVIDER=clerk");
        Assert.hasText(audience, "CLERK_API_AUDIENCE is required when AUTH_PROVIDER=clerk");

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri)
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .build();
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains(audience)
            ? OAuth2TokenValidatorResult.success()
            : OAuth2TokenValidatorResult.failure(new OAuth2Error(
                "invalid_token", "The token audience is not authorized for this API.", null));
        decoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer(issuer), audienceValidator));
        return decoder;
    }
}
