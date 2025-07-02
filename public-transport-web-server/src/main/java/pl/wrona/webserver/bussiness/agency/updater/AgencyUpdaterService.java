package pl.wrona.webserver.bussiness.agency.updater;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.AgencyDetails;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.security.AppUser;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class AgencyUpdaterService {

    private final AgencyRepository agencyRepository;

    @Transactional
    @PreAgencyAuthorize
    public Status updateAgency(String instance, AgencyDetails agencyDetails) {
        AgencyEntity agencyEntity = agencyRepository.findByAgencyCodeEquals(instance);
        agencyEntity.setAgencyName(agencyDetails.getAgencyName());
        agencyEntity.setAgencyUrl(agencyDetails.getAgencyUrl());
        agencyEntity.setAgencyTimetableUrl(agencyDetails.getAgencyTimetableUrl());

        agencyRepository.save(agencyEntity);

        return new Status()
                .status(Status.StatusEnum.SUCCESS);
    }
}
