package pl.wrona.webserver.security.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public record KeycloakProperties(
        String clientId,
        String clientSecret,
        String provisioningClientId,
        String provisioningClientSecret
) {
}
