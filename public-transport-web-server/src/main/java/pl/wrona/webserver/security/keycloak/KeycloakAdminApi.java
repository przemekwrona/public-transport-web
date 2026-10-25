package pl.wrona.webserver.security.keycloak;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Component
class KeycloakAdminApi {

    static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {
    };
    static final ParameterizedTypeReference<List<Map<String, Object>>> LIST = new ParameterizedTypeReference<>() {
    };

    private static final String CERTS_SUFFIX = "/protocol/openid-connect/certs";
    private static final String TOKEN_SUFFIX = "/protocol/openid-connect/token";
    private static final String REALMS_MARKER = "/realms/";

    private final RestClient keycloakRestClient;
    private final KeycloakProperties properties;
    private final OAuth2ResourceServerProperties.Jwt jwt;

    KeycloakAdminApi(RestClient keycloakRestClient,
                     KeycloakProperties properties,
                     OAuth2ResourceServerProperties resourceServerProperties) {
        this.keycloakRestClient = keycloakRestClient;
        this.properties = properties;
        this.jwt = resourceServerProperties.getJwt();
    }

    String accessToken() {
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

    String admin(String pathAndQuery) {
        String realmBase = realmBase();
        int index = realmBase.lastIndexOf(REALMS_MARKER);
        if (index < 0) {
            throw new IllegalStateException("spring.security.oauth2.resourceserver.jwt.jwk-set-uri must contain " + REALMS_MARKER);
        }
        return realmBase.substring(0, index) + "/admin" + realmBase.substring(index) + pathAndQuery;
    }

    <T> T get(String accessToken, String uri, ParameterizedTypeReference<T> bodyType) {
        return keycloakRestClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(bodyType);
    }

    ResponseEntity<Void> post(String accessToken, String uri, Object body) {
        return keycloakRestClient.post()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    void put(String accessToken, String uri) {
        keycloakRestClient.put()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .toBodilessEntity();
    }

    private String tokenUri() {
        return realmBase() + TOKEN_SUFFIX;
    }

    private String realmBase() {
        String jwkSetUri = jwt.getJwkSetUri();
        if (jwkSetUri == null || !jwkSetUri.endsWith(CERTS_SUFFIX)) {
            throw new IllegalStateException("spring.security.oauth2.resourceserver.jwt.jwk-set-uri must end with " + CERTS_SUFFIX);
        }
        return jwkSetUri.substring(0, jwkSetUri.length() - CERTS_SUFFIX.length());
    }

}
