package pl.wrona.webserver.security.keycloak;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import pl.wrona.webserver.exception.BusinessException;

import java.util.List;
import java.util.Map;

@Component
public class KeycloakUserProvisioner {

    private final KeycloakAdminApi keycloakAdminApi;

    KeycloakUserProvisioner(KeycloakAdminApi keycloakAdminApi) {
        this.keycloakAdminApi = keycloakAdminApi;
    }

    public void createUser(String username, String email, String password) {
        try {
            keycloakAdminApi.post(keycloakAdminApi.accessToken(), keycloakAdminApi.admin("/users"), userRepresentation(username, email, password));
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 409) {
                throw new BusinessException("ERROR:202610240030", "User already exists in Keycloak");
            }
            throw new IllegalStateException("Keycloak user provisioning failed with HTTP " + exception.getStatusCode().value(), exception);
        }
    }

    public void changePassword(String username, String newPassword) {
        String accessToken = keycloakAdminApi.accessToken();
        String userId = userId(accessToken, username);
        try {
            keycloakAdminApi.put(accessToken, keycloakAdminApi.admin("/users/" + userId + "/reset-password"), Map.of(
                    "type", "password",
                    "value", newPassword,
                    "temporary", false));
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 400) {
                throw new BusinessException("ERROR:202611060004", "New password was rejected");
            }
            throw new IllegalStateException("Keycloak password change failed with HTTP " + exception.getStatusCode().value(), exception);
        }
    }

    private String userId(String accessToken, String username) {
        String uri = UriComponentsBuilder.fromUriString(keycloakAdminApi.admin("/users"))
                .queryParam("username", username)
                .queryParam("exact", "true")
                .build()
                .toUriString();
        List<Map<String, Object>> users = keycloakAdminApi.get(accessToken, uri, KeycloakAdminApi.LIST);
        if (users != null) {
            for (Map<String, Object> user : users) {
                if (username.equals(user.get("username")) && user.get("id") instanceof String id && !id.isBlank()) {
                    return id;
                }
            }
        }
        throw new BusinessException("ERROR:202610240300", "User " + username + " does not exist in Keycloak");
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

}
