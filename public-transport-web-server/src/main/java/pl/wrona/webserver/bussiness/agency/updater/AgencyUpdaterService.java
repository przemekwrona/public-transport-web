package pl.wrona.webserver.bussiness.agency.updater;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.AgencyDetails;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.agency.address.AgencyAddressUpdaterService;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AgencyUpdaterService {

    private final AgencyRepository agencyRepository;
    private final AgencyAddressUpdaterService agencyAddressUpdaterService;

    @Transactional
    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.AGENCY_ADMIN
    })
    public Status updateAgency(String instance, AgencyDetails agencyDetails) {
        AgencyEntity agencyEntity = agencyRepository.findByAgencyCodeEquals(instance);
        agencyEntity.setAgencyName(agencyDetails.getAgencyName());
        agencyEntity.setAgencyUrl(agencyDetails.getAgencyUrl());
        agencyEntity.setAgencyTimetableUrl(agencyDetails.getAgencyTimetableUrl());

        Optional.ofNullable(agencyDetails.getAgencyAddress())
                .ifPresent(agencyAddress -> agencyAddressUpdaterService.apply(agencyEntity, agencyAddress));

        agencyRepository.save(agencyEntity);

        return new Status()
                .status(Status.StatusEnum.SUCCESS);
    }
}
