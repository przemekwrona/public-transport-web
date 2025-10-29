package pl.wrona.webserver.bussiness.admin.profile.creator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.AgencyAdminCreateAccountRequest;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.client.geoapify.geocode.Feature;
import pl.wrona.webserver.client.geoapify.GeoapifyService;
import pl.wrona.webserver.client.geoapify.geocode.Geometry;
import pl.wrona.webserver.security.AppRole;
import pl.wrona.webserver.security.AppUser;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class ProfileCreatorService {

    public static final String SUPER_USER = "SUPER_USER";

    private final ProfileCreatorAppUserRepository profileCreatorAppUserRepository;
    private final ProfileCreatorAgencyRepository profileCreatorAgencyRepository;
    private final ProfileCreatorAppRolesRepository profileCreatorAppRolesRepository;
    private final GeoapifyService geoapifyService;

    @Transactional
    public Status createNewAccount(AgencyAdminCreateAccountRequest agencyAdminCreateAccountRequest) {
        Feature addressFeature = geoapifyService.mostProbableAddress(agencyAdminCreateAccountRequest.getStreet(), agencyAdminCreateAccountRequest.getHouseNumber(), agencyAdminCreateAccountRequest.getFlatNumber(), agencyAdminCreateAccountRequest.getPostalCode(), agencyAdminCreateAccountRequest.getPostalCity());

        AppUser user = profileCreatorAppUserRepository.findAppUsersByUsername(agencyAdminCreateAccountRequest.getAccountName());
        Set<AppUser> superUsers = findSuperUsers();
        Set<AppUser> agencyVisibility = new HashSet<>(superUsers);
        agencyVisibility.add(user);

        AgencyEntity agencyEntity = new AgencyEntity();
        agencyEntity.setAgencyName(agencyAdminCreateAccountRequest.getCompanyName());
        agencyEntity.setAgencyCode(agencyAdminCreateAccountRequest.getCompanyCode());
        agencyEntity.setAppUser(user);
        agencyEntity.setTexNumber(agencyAdminCreateAccountRequest.getTaxNumber());
        agencyEntity.setStreet(agencyAdminCreateAccountRequest.getStreet());
        agencyEntity.setHouseNumber(agencyAdminCreateAccountRequest.getHouseNumber());
        agencyEntity.setFlatNumber(agencyAdminCreateAccountRequest.getFlatNumber());
        agencyEntity.setPostalCode(agencyAdminCreateAccountRequest.getPostalCode());
        agencyEntity.setPostalCity(agencyAdminCreateAccountRequest.getPostalCity());
        Optional.of(addressFeature).map(Feature::geometry).map(Geometry::coordinates).ifPresent(cords -> {
            agencyEntity.setLatitude(cords.get(1));
            agencyEntity.setLongitude(cords.get(0));
        });
        agencyEntity.setUsers(agencyVisibility);
        agencyEntity.setCreatedAt(LocalDateTime.now());

        AgencyEntity savedAgencyEntity = profileCreatorAgencyRepository.save(agencyEntity);

        return new Status().status(Status.StatusEnum.CREATED);
    }

    private Set<AppUser> findSuperUsers() {
        Set<AppRole> superUserRoles = profileCreatorAppRolesRepository.findAllByRoleIsIn(Set.of(SUPER_USER));
        return profileCreatorAppUserRepository.findByAppRolesIsIn(superUserRoles);
    }

}
