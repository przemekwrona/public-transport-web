package pl.wrona.webserver.keycloak;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.security.AppUser;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "keycloak", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KeycloakIdentityProvisionerImpl implements KeycloakIdentityProvisioner {

    static final List<String> AGENCY_SUBGROUPS = List.of("owners", "planners", "dispatchers", "viewers");

    private static final Map<String, String> SUBGROUP_CLIENT_ROLES = Map.of(
            "owners", "owner",
            "planners", "planner",
            "dispatchers", "dispatcher",
            "viewers", "viewer"
    );

    private final KeycloakAdminClient keycloakAdminClient;

    public KeycloakIdentityProvisionerImpl(KeycloakAdminClient keycloakAdminClient) {
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @Override
    public void provisionAgency(String agencyCode, AppUser owner) {
        String agenciesGroupId = keycloakAdminClient.ensureGroup(null, "agencies", Map.of());
        String agencyGroupId = keycloakAdminClient.ensureGroup(agenciesGroupId, agencyCode, Map.of("agency_code", List.of(agencyCode)));

        String ownersGroupId = null;
        for (String subgroup : AGENCY_SUBGROUPS) {
            String groupId = keycloakAdminClient.ensureGroup(agencyGroupId, subgroup, Map.of());
            String clientRole = SUBGROUP_CLIENT_ROLES.get(subgroup);
            if (clientRole != null) {
                keycloakAdminClient.assignClientRole(groupId, clientRole);
            }
            if ("owners".equals(subgroup)) {
                ownersGroupId = groupId;
            }
        }

        String userId = keycloakAdminClient.ensureUser(owner.getUsername(), owner.getEmail(), owner.getPassword());
        keycloakAdminClient.addUserToGroup(userId, ownersGroupId);
    }
}
