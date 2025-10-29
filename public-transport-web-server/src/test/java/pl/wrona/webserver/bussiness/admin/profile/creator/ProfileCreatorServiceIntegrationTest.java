package pl.wrona.webserver.bussiness.admin.profile.creator;

import org.igeolab.iot.pt.server.api.model.AgencyAdminCreateAccountRequest;
import org.igeolab.iot.pt.server.api.model.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.wrona.webserver.BaseIntegrationTest;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.security.AppRole;
import pl.wrona.webserver.security.AppRoleRepository;
import pl.wrona.webserver.security.AppUser;
import pl.wrona.webserver.security.AppUserRepository;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileCreatorServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AgencyRepository agencyRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AppRoleRepository appRoleRepository;

    @Autowired
    private ProfileCreatorService profileCreatorService;

    @Test
    void shouldCreateProfile() {
        // given
        var appUsername = "jkowalski";
        var agencyCode = "NS";
        var createAgencyRequest = new AgencyAdminCreateAccountRequest()
                .companyName("Następna Stacja")
                .companyCode(agencyCode)
                .taxNumber("5267791688")
                .accountName(appUsername)
                .street("Nakielska")
                .houseNumber("3")
                .flatNumber("1")
                .postalCode("01-106")
                .postalCity("Warszawa");

        // when
        Status status = profileCreatorService.createNewAccount(createAgencyRequest);

        // then
        assertThat(status).isNotNull().isEqualTo(new Status().status(Status.StatusEnum.CREATED));

        AgencyEntity createdAgencyEntity = agencyRepository.findByAgencyCodeEquals(agencyCode);
        assertThat(createdAgencyEntity).isNotNull();
        assertThat(createdAgencyEntity.getAgencyName()).isEqualTo("Następna Stacja");
        assertThat(createdAgencyEntity.getAgencyCode()).isEqualTo("NS");
        assertThat(createdAgencyEntity.getTexNumber()).isEqualTo("5267791688");
        assertThat(createdAgencyEntity.getStreet()).isEqualTo("Nakielska");
        assertThat(createdAgencyEntity.getHouseNumber()).isEqualTo("3");
        assertThat(createdAgencyEntity.getFlatNumber()).isEqualTo("1");
        assertThat(createdAgencyEntity.getPostalCode()).isEqualTo("01-106");
        assertThat(createdAgencyEntity.getPostalCity()).isEqualTo("Warszawa");
        assertThat(createdAgencyEntity.getLongitude()).isEqualTo(20.9383168);
        assertThat(createdAgencyEntity.getLatitude()).isEqualTo(52.230207);
        assertThat(createdAgencyEntity.getUsers().stream().map(AppUser::getUsername).toList()).hasSize(2).hasSameElementsAs(List.of(appUsername, "pwrona"));

        Set<AppRole> createdUserAppRoles = appRoleRepository.findAppRolesByRoleIsIn(Set.of("AGENCY_OWNER"));
        AppUser appUser = appUserRepository.findByUsername(appUsername).orElse(null);
        assertThat(appUser).isNotNull();
        assertThat(appUser.getUsername()).isEqualTo(appUsername);
        assertThat(appUser.getAppRoles()).hasSize(1).hasSameElementsAs(createdUserAppRoles);
    }

}