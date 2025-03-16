package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.AgencyDetails;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.agency.entity.Agency;
import pl.wrona.webserver.security.AppUser;

@Service
@AllArgsConstructor
public class AgencyService {

    private final AgencyRepository agencyRepository;

    public Agency getLoggedAgency() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        return agencyRepository.findByAppUser(appUser);
    }

    public AgencyDetails getAgency() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        Agency agency = agencyRepository.findByAppUser(appUser);
        return new AgencyDetails()
                .agencyName(agency.getAgencyName())
                .agencyUrl(agency.getAgencyUrl())
                .agencyTimetableUrl(agency.getAgencyTimetableUrl());
    }

    @Transactional
    public Status updateAgency(AgencyDetails agencyDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        Agency agency = agencyRepository.findByAppUser(appUser);
        agency.setAgencyName(agencyDetails.getAgencyName());
        agency.setAgencyUrl(agencyDetails.getAgencyUrl());
        agency.setAgencyTimetableUrl(agencyDetails.getAgencyTimetableUrl());

        agencyRepository.save(agency);

        return new Status()
                .status(Status.StatusEnum.SUCCESS);
    }

    public Agency findAgencyByAppUser(AppUser appUser) {
        return agencyRepository.findByAppUser(appUser);
    }
}
