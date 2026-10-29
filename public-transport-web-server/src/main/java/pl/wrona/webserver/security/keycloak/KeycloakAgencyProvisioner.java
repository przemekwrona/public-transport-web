package pl.wrona.webserver.security.keycloak;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@Component
public class KeycloakAgencyProvisioner {

    private static final String AGENCIES = "agencies";
    private static final String AGENCY_OWNER = PreAgencyAuthorize.Role.AGENCY_OWNER.name();
    private static final String SUPER_USER = PreAgencyAuthorize.Role.SUPER_USER.name();

    private final KeycloakAdminApi keycloakAdminApi;

    KeycloakAgencyProvisioner(KeycloakAdminApi keycloakAdminApi) {
        this.keycloakAdminApi = keycloakAdminApi;
    }

    public void createAgency(String agencyCode, String ownerUsername, Collection<String> superUsernames) {
        String accessToken = keycloakAdminApi.accessToken();
        Map<String, Set<String>> rolesByUsername = rolesByUsername(ownerUsername, superUsernames);
        Map<String, String> userIds = new LinkedHashMap<>();
        rolesByUsername.keySet().forEach(username -> userIds.put(username, userId(accessToken, username)));

        String agenciesGroupId = agenciesGroupId(accessToken);
        String agencyGroupId = createChild(accessToken, agenciesGroupId, agencyCode, true);
        Map<String, String> roleGroupIds = new LinkedHashMap<>();
        for (PreAgencyAuthorize.Role role : PreAgencyAuthorize.Role.values()) {
            roleGroupIds.put(role.name(), createChild(accessToken, agencyGroupId, role.name(), false));
        }

        rolesByUsername.forEach((username, roles) -> {
            join(accessToken, userIds.get(username), agencyGroupId);
            roles.forEach(role -> join(accessToken, userIds.get(username), roleGroupIds.get(role)));
        });
    }

    private Map<String, Set<String>> rolesByUsername(String ownerUsername, Collection<String> superUsernames) {
        if (ownerUsername == null || ownerUsername.isBlank()) {
            throw new BusinessException("ERROR:202610240301", "Agency owner is missing");
        }
        Map<String, Set<String>> rolesByUsername = new TreeMap<>();
        rolesByUsername.computeIfAbsent(ownerUsername, ignored -> new TreeSet<>()).add(AGENCY_OWNER);
        if (superUsernames != null) {
            for (String username : superUsernames) {
                if (username == null || username.isBlank()) {
                    continue;
                }
                rolesByUsername.computeIfAbsent(username, ignored -> new TreeSet<>()).add(SUPER_USER);
            }
        }
        return rolesByUsername;
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

    private String agenciesGroupId(String accessToken) {
        String uri = UriComponentsBuilder.fromUriString(keycloakAdminApi.admin("/groups"))
                .queryParam("search", AGENCIES)
                .queryParam("exact", "true")
                .build()
                .toUriString();
        List<Map<String, Object>> groups = keycloakAdminApi.get(accessToken, uri, KeycloakAdminApi.LIST);
        if (groups != null) {
            for (Map<String, Object> group : groups) {
                if (AGENCIES.equals(group.get("name"))
                        && (group.get("path") == null || "/agencies".equals(group.get("path")))
                        && group.get("id") instanceof String id
                        && !id.isBlank()) {
                    return id;
                }
            }
        }
        return idFromLocation(keycloakAdminApi.post(accessToken, keycloakAdminApi.admin("/groups"), Map.of("name", AGENCIES)).getHeaders().getLocation());
    }

    private String createChild(String accessToken, String parentId, String name, boolean conflictIsAgency) {
        try {
            ResponseEntity<Void> created = keycloakAdminApi.post(
                    accessToken,
                    keycloakAdminApi.admin("/groups/" + parentId + "/children"),
                    Map.of("name", name));
            return idFromLocation(created.getHeaders().getLocation());
        } catch (RestClientResponseException exception) {
            if (conflictIsAgency && exception.getStatusCode().value() == 409) {
                throw new BusinessException("ERROR:202610240259", "Agency already exists in Keycloak");
            }
            throw new IllegalStateException("Keycloak group provisioning failed with HTTP " + exception.getStatusCode().value(), exception);
        }
    }

    private void join(String accessToken, String userId, String groupId) {
        keycloakAdminApi.put(accessToken, keycloakAdminApi.admin("/users/" + userId + "/groups/" + groupId));
    }

    private static String idFromLocation(URI location) {
        if (location == null || location.getPath() == null) {
            throw new IllegalStateException("Keycloak did not return the created group");
        }
        String path = location.getPath();
        int slash = path.lastIndexOf('/');
        if (slash < 0 || slash == path.length() - 1) {
            throw new IllegalStateException("Keycloak did not return the created group");
        }
        return path.substring(slash + 1);
    }

}
