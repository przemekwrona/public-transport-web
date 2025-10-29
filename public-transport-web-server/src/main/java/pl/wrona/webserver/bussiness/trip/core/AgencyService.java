package pl.wrona.webserver.bussiness.trip.core;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.trip.core.agency.AgencyEntity;
import pl.wrona.webserver.security.AppUser;
import org.igeolab.iot.pt.server.api.model.AgenciesAdminResponse;
import org.igeolab.iot.pt.server.api.model.AgencyAdminDetail;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AgencyService {

    private final AgencyRepository agencyRepository;

    public AgencyEntity findAgencyByAgencyCode(String agencyCode) {
        return agencyRepository.findByAgencyCodeEquals(agencyCode);
    }

    @Deprecated
    @Transactional(readOnly = true)
    public AgencyEntity getLoggedAgency() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        return agencyRepository.findByAppUser(appUser).orElse(null);
    }

    public AgenciesAdminResponse findAllAgencies() {
        var agencies = agencyRepository.countRoutesByAgency().stream().map(groupedAgency -> new AgencyAdminDetail()
                        .agencyCode(groupedAgency.getAgency().getAgencyCode())
                        .agencyName(groupedAgency.getAgency().getAgencyName())
                        .createdAt(Optional.of(groupedAgency.getAgency()).map(AgencyEntity::getCreatedAt).orElse(null))
                        .routesNo(groupedAgency.getRouteCount()))
                .toList();

        return new AgenciesAdminResponse()
                .agencies(agencies);
    }
}
