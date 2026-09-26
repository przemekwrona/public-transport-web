package pl.wrona.webserver.keycloak;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrona.webserver.security.AppUser;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakIdentityProvisionerImplTest {

    @Mock
    private KeycloakAdminClient keycloakAdminClient;

    @Test
    void createsAgencyGroupAndAddsOwner() {
        when(keycloakAdminClient.ensureGroup(null, "agencies", Map.of())).thenReturn("agencies-id");
        when(keycloakAdminClient.ensureGroup(eq("agencies-id"), eq("NS"), any())).thenReturn("agency-id");
        when(keycloakAdminClient.ensureGroup(eq("agency-id"), eq("owners"), any())).thenReturn("owners-id");
        when(keycloakAdminClient.ensureGroup(eq("agency-id"), eq("planners"), any())).thenReturn("planners-id");
        when(keycloakAdminClient.ensureGroup(eq("agency-id"), eq("dispatchers"), any())).thenReturn("dispatchers-id");
        when(keycloakAdminClient.ensureGroup(eq("agency-id"), eq("viewers"), any())).thenReturn("viewers-id");
        when(keycloakAdminClient.ensureUser("jkowalski", "jan@example.com", "$2a$10$hash")).thenReturn("user-id");

        AppUser owner = AppUser.builder()
                .username("jkowalski")
                .email("jan@example.com")
                .password("$2a$10$hash")
                .build();

        new KeycloakIdentityProvisionerImpl(keycloakAdminClient).provisionAgency("NS", owner);

        verify(keycloakAdminClient).assignClientRole("owners-id", "AGENCY_OWNER");
        verify(keycloakAdminClient).assignClientRole("planners-id", "planner");
        verify(keycloakAdminClient).assignClientRole("dispatchers-id", "dispatcher");
        verify(keycloakAdminClient).assignClientRole("viewers-id", "viewer");
        verify(keycloakAdminClient).addUserToGroup("user-id", "owners-id");
    }
}
