package pl.wrona.webserver.security.keycloak;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import pl.wrona.webserver.exception.BusinessException;

import java.util.List;
import java.util.Map;

@Component
public class KeycloakUserProvisioner {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {
    };
    private static final String CERTS_SUFFIX = "/protocol/openid-connect/certs";
    private static final String TOKEN_SUFFIX = "/protocol/openid-connect/token";
    private static final String REALMS_MARKER = "/realms/";

    private final RestClient keycloakRestClient;
    private final KeycloakProperties properties;
    private final OAuth2ResourceServerProperties.Jwt jwt;

    KeycloakUserProvisioner(RestClient keycloakRestClient,
                            KeycloakProperties properties,
                            OAuth2ResourceServerProperties resourceServerProperties) {
        this.keycloakRestClient = keycloakRestClient;
        this.properties = properties;
        this.jwt = resourceServerProperties.getJwt();
    }

    public void createUser(String username, String email, String password) {
        try {
            keycloakRestClient.post()
                    .uri(adminUsersUri())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(userRepresentation(username, email, password))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 409) {
                throw new BusinessException("ERROR:202610240030", "User already exists in Keycloak");
            }
            throw new IllegalStateException("Keycloak user provisioning failed with HTTP " + exception.getStatusCode().value(), exception);
        }
    }

    private String accessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.provisioningClientId());
        form.add("client_secret", properties.provisioningClientSecret());
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
            throw new IllegalStateException("Keycloak provisioning login failed with HTTP " + exception.getStatusCode().value(), exception);
        }
    }

    private Map<String, Object> userRepresentation(String username, String email, String password) {
        return Map.of(
                "username", username,
                "email", email,
                "enabled", true,
                "emailVerified", true,
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", password,
                        "temporary", false)));
    }

    private String tokenUri() {
        return realmBase() + TOKEN_SUFFIX;
    }

    private String adminUsersUri() {
        String realmBase = realmBase();
        int index = realmBase.lastIndexOf(REALMS_MARKER);
        if (index < 0) {
            throw new IllegalStateException("spring.security.oauth2.resourceserver.jwt.jwk-set-uri must contain " + REALMS_MARKER);
        }
        return realmBase.substring(0, index) + "/admin" + realmBase.substring(index) + "/users";
    }

    private String realmBase() {
        String jwkSetUri = jwt.getJwkSetUri();
        if (jwkSetUri == null || !jwkSetUri.endsWith(CERTS_SUFFIX)) {
            throw new IllegalStateException("spring.security.oauth2.resourceserver.jwt.jwk-set-uri must end with " + CERTS_SUFFIX);
        }
        return jwkSetUri.substring(0, jwkSetUri.length() - CERTS_SUFFIX.length());
    }

}
