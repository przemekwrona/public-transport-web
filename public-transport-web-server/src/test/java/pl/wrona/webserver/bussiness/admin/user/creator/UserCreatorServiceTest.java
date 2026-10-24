package pl.wrona.webserver.bussiness.admin.user.creator;

import org.igeolab.iot.pt.server.api.model.CreateAppUserRequest;
import org.igeolab.iot.pt.server.api.model.CreateAppUserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.wrona.webserver.security.AppUser;
import pl.wrona.webserver.security.AppUserRepository;
import pl.wrona.webserver.security.keycloak.KeycloakUserProvisioner;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserCreatorServiceTest {

    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final AppUserRepository appUserRepository = mock(AppUserRepository.class);
    private final UserCreatorAppRolesRepository rolesRepository = mock(UserCreatorAppRolesRepository.class);
    private final KeycloakUserProvisioner keycloakUserProvisioner = mock(KeycloakUserProvisioner.class);
    private final UserCreatorService userCreatorService = new UserCreatorService(
            encoder, appUserRepository, rolesRepository, keycloakUserProvisioner);

    @Test
    void storesTheAccountAndCreatesItInKeycloak() {
        when(encoder.encode("welcome1")).thenReturn("hash");
        when(rolesRepository.findAllByRoleIsIn(Set.of("AGENCY_OWNER"))).thenReturn(Set.of());
        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateAppUserResponse response = userCreatorService.createUser(new CreateAppUserRequest()
                .username("jan")
                .email("jan@example.com")
                .password("welcome1"));

        assertThat(response.getUsername()).isEqualTo("jan");
        verify(keycloakUserProvisioner).createUser("jan", "jan@example.com", "welcome1");
    }

}
