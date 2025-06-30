package pl.wrona.webserver.core;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.AgencyDetails;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.security.AppUser;
import org.igeolab.iot.pt.server.api.model.AgenciesAdminResponse;
import org.igeolab.iot.pt.server.api.model.AgencyAdminDetail;

import java.time.ZoneOffset;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AgencyService {

    private final AgencyRepository agencyRepository;

    @Transactional(readOnly = true)
    public AgencyEntity getLoggedAgency() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        return agencyRepository.findByAppUser(appUser);
    }

    public AgencyDetails getAgency() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        AgencyEntity agencyEntity = agencyRepository.findByAppUser(appUser);
        return new AgencyDetails()
                .agencyName(agencyEntity.getAgencyName())
                .agencyUrl(agencyEntity.getAgencyUrl())
                .agencyTimetableUrl(agencyEntity.getAgencyTimetableUrl());
    }

    @Transactional
    public Status updateAgency(AgencyDetails agencyDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        AgencyEntity agencyEntity = agencyRepository.findByAppUser(appUser);
        agencyEntity.setAgencyName(agencyDetails.getAgencyName());
        agencyEntity.setAgencyUrl(agencyDetails.getAgencyUrl());
        agencyEntity.setAgencyTimetableUrl(agencyDetails.getAgencyTimetableUrl());

        agencyRepository.save(agencyEntity);

        return new Status()
                .status(Status.StatusEnum.SUCCESS);
    }

    public AgencyEntity findAgencyByAppUser(AppUser appUser) {
        return agencyRepository.findByAppUser(appUser);
    }

    public AgenciesAdminResponse findAllAgencies() {
        var agencies = agencyRepository.countRoutesByAgency().stream().map(groupedAgency -> new AgencyAdminDetail()
                        .agencyCode(groupedAgency.getAgency().getAgencyCode())
                        .agencyName(groupedAgency.getAgency().getAgencyName())
                        .createdAt(Optional.of(groupedAgency.getAgency()).map(AgencyEntity::getCreatedAt).map(createdAt -> createdAt.atOffset(ZoneOffset.UTC)).orElse(null))
                        .routesNo(groupedAgency.getRouteCount()))
                .toList();

        return new AgenciesAdminResponse()
                .agencies(agencies);
    }
}
