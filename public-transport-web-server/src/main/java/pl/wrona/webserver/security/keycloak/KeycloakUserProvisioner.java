package pl.wrona.webserver.security.keycloak;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
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
