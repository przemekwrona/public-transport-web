package pl.wrona.webserver.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class KeycloakPasswordGrant {

    private final RestClient restClient;
    private final KeycloakProperties properties;
    private final ObjectProvider<KeycloakAdminClient> keycloakAdminClient;
    private volatile boolean loginClientEnsured;

    @Autowired
    public KeycloakPasswordGrant(KeycloakProperties properties, ObjectProvider<KeycloakAdminClient> keycloakAdminClient) {
        this(RestClient.builder().baseUrl(properties.getServerUrl()).build(), properties, keycloakAdminClient);
    }

    KeycloakPasswordGrant(RestClient restClient, KeycloakProperties properties, ObjectProvider<KeycloakAdminClient> keycloakAdminClient) {
        this.restClient = restClient;
        this.properties = properties;
        this.keycloakAdminClient = keycloakAdminClient;
    }

    public String accessToken(String username, String password) {
        return requestToken(username, password, true);
    }

    private String requestToken(String username, String password, boolean retryIncompleteProfile) {
        ensureLoginClient();
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", properties.getLogin().getClientId());
        form.add("client_secret", properties.getLogin().getClientSecret());
        form.add("username", username);
        form.add("password", password);
        try {
            JsonNode token = restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", properties.getRealm())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(JsonNode.class);
            if (token == null || !token.hasNonNull("access_token")) {
                throw new IllegalStateException("Keycloak did not return an access token");
            }
            return token.path("access_token").asText();
        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();
            String body = exception.getResponseBodyAsString();
            if (retryIncompleteProfile && body.contains("Account is not fully set up") && fillMissingProfile(username)) {
                return requestToken(username, password, false);
            }
            if (body.contains("Account is not fully set up")) {
                throw new BadCredentialsException("Keycloak account is not fully set up");
            }
            if ((status == 400 || status == 401) && body.contains("invalid_grant")) {
                throw new BadCredentialsException("Invalid username or password");
            }
            throw new IllegalStateException("Keycloak token request failed: " + status + " " + body, exception);
        }
    }

    private void ensureLoginClient() {
        if (loginClientEnsured) {
            return;
        }
        KeycloakAdminClient adminClient = keycloakAdminClient.getIfAvailable();
        if (adminClient != null) {
            adminClient.ensureLoginClient(properties.getLogin().getClientId(), properties.getLogin().getClientSecret());
        }
        loginClientEnsured = true;
    }

    private boolean fillMissingProfile(String username) {
        KeycloakAdminClient adminClient = keycloakAdminClient.getIfAvailable();
        return adminClient != null && adminClient.fillMissingProfile(username);
    }
}
