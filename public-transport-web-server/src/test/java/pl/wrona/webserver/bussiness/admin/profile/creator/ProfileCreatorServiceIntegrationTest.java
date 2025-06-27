package pl.wrona.webserver.bussiness.admin.profile.creator;

import org.igeolab.iot.pt.server.api.model.AgencyAdminCreateAccountRequest;
import org.igeolab.iot.pt.server.api.model.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.BaseIntegrationTest;
import pl.wrona.webserver.agency.AgencyRepository;
import pl.wrona.webserver.agency.entity.Agency;
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
    @Transactional
    void shouldCreateProfile() {
        // given
        var agencyCode = "NS";
        var createAgencyRequest = new AgencyAdminCreateAccountRequest()
                .companyName("Następna Stacja")
                .companyCode(agencyCode)
                .taxNumber("5267791688")
                .accountName("jkowalski")
                .accountPassword("welcome1")
                .street("Nakielska")
                .houseNumber("3")
                .flatNumber("1")
                .postalCode("01-106")
                .postalCity("Warszawa");

        // when
        Status status = profileCreatorService.createNewAccount(createAgencyRequest);

        // then
        assertThat(status).isNotNull().isEqualTo(new Status().status(Status.StatusEnum.CREATED));

        Agency createdAgency = agencyRepository.findByAgencyCodeEquals(agencyCode);
        assertThat(createdAgency).isNotNull();
        assertThat(createdAgency.getAgencyName()).isEqualTo("Następna Stacja");
        assertThat(createdAgency.getAgencyCode()).isEqualTo("NS");
        assertThat(createdAgency.getTexNumber()).isEqualTo("5267791688");
        assertThat(createdAgency.getStreet()).isEqualTo("Nakielska");
        assertThat(createdAgency.getHouseNumber()).isEqualTo("3");
        assertThat(createdAgency.getFlatNumber()).isEqualTo("1");
        assertThat(createdAgency.getPostalCode()).isEqualTo("01-106");
        assertThat(createdAgency.getPostalCity()).isEqualTo("Warszawa");
        assertThat(createdAgency.getLongitude()).isEqualTo(20.9383168);
        assertThat(createdAgency.getLatitude()).isEqualTo(52.230207);
        assertThat(createdAgency.getUsers().stream().map(AppUser::getUsername).toList()).hasSize(2).hasSameElementsAs(List.of("jkowalski", "pwrona"));

        Set<AppRole> createdUserAppRoles = appRoleRepository.findAppRolesByRoleIsIn(Set.of("AGENCY_OWNER"));
        AppUser appUser = appUserRepository.findByUsername("jkowalski").orElse(null);
        assertThat(appUser).isNotNull();
        assertThat(appUser.getUsername()).isEqualTo("jkowalski");
        assertThat(appUser.getAppRoles()).hasSize(1).hasSameElementsAs(createdUserAppRoles);
    }

}