package pl.wrona.webserver.bussiness.admin.profile.creator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.AgencyAdminCreateAccountRequest;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.agency.entity.Agency;
import pl.wrona.webserver.security.AppRole;
import pl.wrona.webserver.security.AppUser;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@AllArgsConstructor
public class ProfileCreatorService {

    public static final String SUPER_USER = "SUPER_USER";

    private final ProfileCreatorAppUserRepository profileCreatorAppUserRepository;
    private final ProfileCreatorAgencyRepository profileCreatorAgencyRepository;
    private final ProfileCreatorAppRolesRepository profileCreatorAppRolesRepository;

    @Transactional
    public Status createNewAccount(AgencyAdminCreateAccountRequest agencyAdminCreateAccountRequest) {
        AppUser savedAppUser = createAppUser(agencyAdminCreateAccountRequest);

        Set<AppUser> superUsers = findSuperUsers();
        Set<AppUser> agencyVisibility = new HashSet<>(superUsers);
        agencyVisibility.add(savedAppUser);

        Agency agency = new Agency();
        agency.setAgencyName(agencyAdminCreateAccountRequest.getCompanyName());
        agency.setAgencyCode(agencyAdminCreateAccountRequest.getCompanyCode());
        agency.setAppUser(savedAppUser);
        agency.setTexNumber(agencyAdminCreateAccountRequest.getTaxNumber());
        agency.setStreet(agencyAdminCreateAccountRequest.getStreet());
        agency.setHouseNumber(agencyAdminCreateAccountRequest.getHouseNumber());
        agency.setFlatNumber(agencyAdminCreateAccountRequest.getFlatNumber());
        agency.setPostalCode(agencyAdminCreateAccountRequest.getPostalCode());
        agency.setPostalCity(agencyAdminCreateAccountRequest.getPostalCity());
        agency.setUsers(agencyVisibility);
        agency.setCreatedAt(LocalDateTime.now());

        Agency savedAgency = profileCreatorAgencyRepository.save(agency);

        return new Status().status(Status.StatusEnum.CREATED);
    }

    private Set<AppUser> findSuperUsers() {
        Set<AppRole> superUserRoles = profileCreatorAppRolesRepository.findAllByRoleIsIn(Set.of(SUPER_USER));
        return profileCreatorAppUserRepository.findByAppRolesIsIn(superUserRoles);
    }

    private AppUser createAppUser(AgencyAdminCreateAccountRequest agencyAdminCreateAccountRequest) {
        Set<AppRole> defaultAgencyRole = profileCreatorAppRolesRepository.findAllByRoleIsIn(Set.of("AGENCY_OWNER"));

        AppUser appUser = new AppUser();
        appUser.setCreatedAt(LocalDateTime.now());
        appUser.setUpdatedAt(LocalDateTime.now());
        appUser.setUsername(agencyAdminCreateAccountRequest.getAccountName());
        appUser.setPassword(agencyAdminCreateAccountRequest.getAccountPassword());
        appUser.setAppRoles(defaultAgencyRole);

        return profileCreatorAppUserRepository.save(appUser);
    }
}
