package pl.wrona.webserver.security.keycloak;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Component
public class KeycloakLoginClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {
    };
    private static final String CERTS_SUFFIX = "/protocol/openid-connect/certs";
    private static final String TOKEN_SUFFIX = "/protocol/openid-connect/token";

    private final RestClient keycloakRestClient;
    private final KeycloakProperties properties;
    private final OAuth2ResourceServerProperties.Jwt jwt;

    KeycloakLoginClient(RestClient keycloakRestClient,
                        KeycloakProperties properties,
                        OAuth2ResourceServerProperties resourceServerProperties) {
        this.keycloakRestClient = keycloakRestClient;
        this.properties = properties;
        this.jwt = resourceServerProperties.getJwt();
    }

    public String login(String username, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        form.add("username", username);
        form.add("password", password);
        try {
            Map<String, Object> body = keycloakRestClient.post()
                    .uri(tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(MAP);
            if (body == null || !(body.get("access_token") instanceof String accessToken) || accessToken.isBlank()) {
                throw new IllegalStateException("Keycloak did not return an access token");
            }
            return accessToken;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 400 || exception.getStatusCode().value() == 401) {
                throw new BadCredentialsException("Invalid credentials");
            }
            throw new IllegalStateException("Keycloak login failed with HTTP " + exception.getStatusCode().value(), exception);
        }
    }

    private String tokenUri() {
        String jwkSetUri = jwt.getJwkSetUri();
        if (jwkSetUri == null || !jwkSetUri.endsWith(CERTS_SUFFIX)) {
            throw new IllegalStateException("spring.security.oauth2.resourceserver.jwt.jwk-set-uri must end with " + CERTS_SUFFIX);
        }
        return jwkSetUri.substring(0, jwkSetUri.length() - CERTS_SUFFIX.length()) + TOKEN_SUFFIX;
    }

}
