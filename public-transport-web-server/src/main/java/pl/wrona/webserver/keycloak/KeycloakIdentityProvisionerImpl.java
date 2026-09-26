package pl.wrona.webserver.keycloak;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.security.AppUser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "keycloak", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KeycloakIdentityProvisionerImpl implements KeycloakIdentityProvisioner {

    static final Map<String, String> AGENCY_SUBGROUPS = new LinkedHashMap<>();

    static {
        AGENCY_SUBGROUPS.put("owners", "AGENCY_OWNER");
        AGENCY_SUBGROUPS.put("planners", "planner");
        AGENCY_SUBGROUPS.put("dispatchers", "dispatcher");
        AGENCY_SUBGROUPS.put("viewers", "viewer");
    }

    private final KeycloakAdminClient keycloakAdminClient;

    public KeycloakIdentityProvisionerImpl(KeycloakAdminClient keycloakAdminClient) {
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @Override
    public void provisionAgency(String agencyCode, AppUser owner) {
        String agenciesGroupId = keycloakAdminClient.ensureGroup(null, "agencies", Map.of());
        String agencyGroupId = keycloakAdminClient.ensureGroup(agenciesGroupId, agencyCode, Map.of("agency_code", List.of(agencyCode)));

        String ownersGroupId = null;
        for (Map.Entry<String, String> subgroup : AGENCY_SUBGROUPS.entrySet()) {
            String groupId = keycloakAdminClient.ensureGroup(agencyGroupId, subgroup.getKey(), Map.of());
            keycloakAdminClient.assignClientRole(groupId, subgroup.getValue());
            if ("owners".equals(subgroup.getKey())) {
                ownersGroupId = groupId;
            }
        }

        String userId = keycloakAdminClient.ensureUser(owner.getUsername(), owner.getEmail(), owner.getPassword());
        keycloakAdminClient.addUserToGroup(userId, ownersGroupId);
    }
}
