package pl.wrona.webserver.bussiness.admin.profile.creator;

import org.igeolab.iot.pt.server.api.model.AgencyAdminCreateAccountRequest;
import org.igeolab.iot.pt.server.api.model.Status;
import org.junit.jupiter.api.Test;
import pl.wrona.webserver.client.geoapify.GeoapifyService;
import pl.wrona.webserver.client.geoapify.geocode.Feature;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.security.AppUser;
import pl.wrona.webserver.security.keycloak.KeycloakAgencyProvisioner;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileCreatorServiceTest {

    private final ProfileCreatorAppUserRepository users = mock(ProfileCreatorAppUserRepository.class);
    private final ProfileCreatorAgencyRepository agencies = mock(ProfileCreatorAgencyRepository.class);
    private final ProfileCreatorAppRolesRepository roles = mock(ProfileCreatorAppRolesRepository.class);
    private final GeoapifyService geoapifyService = mock(GeoapifyService.class);
    private final KeycloakAgencyProvisioner keycloakAgencyProvisioner = mock(KeycloakAgencyProvisioner.class);
    private final ProfileCreatorService profileCreatorService = new ProfileCreatorService(
            users, agencies, roles, geoapifyService, keycloakAgencyProvisioner);

    @Test
    void createsTheAgencyGroupInKeycloak() {
        AppUser owner = new AppUser();
        owner.setUsername("jan");
        AppUser superUser = new AppUser();
        superUser.setUsername("pwrona");
        when(geoapifyService.mostProbableAddress("Nakielska", "3", "1", "01-106", "Warszawa"))
                .thenReturn(new Feature(null, null, null, null));
        when(users.findAppUsersByUsername("jan")).thenReturn(owner);
        when(roles.findAllByRoleIsIn(Set.of(ProfileCreatorService.SUPER_USER))).thenReturn(Set.of());
        when(users.findByAppRolesIsIn(any())).thenReturn(Set.of(superUser));
        when(agencies.save(any(AgencyEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Status status = profileCreatorService.createNewAccount(new AgencyAdminCreateAccountRequest()
                .companyName("Następna Stacja")
                .companyCode("NS")
                .accountName("jan")
                .street("Nakielska")
                .houseNumber("3")
                .flatNumber("1")
                .postalCode("01-106")
                .postalCity("Warszawa"));

        assertThat(status.getStatus()).isEqualTo(Status.StatusEnum.CREATED);
        verify(keycloakAgencyProvisioner).createAgency("NS", "jan", Set.of("pwrona"));
    }

}
