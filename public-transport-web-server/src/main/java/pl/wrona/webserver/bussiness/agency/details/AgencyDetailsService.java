package pl.wrona.webserver.bussiness.agency.details;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.AgencyAddress;
import org.igeolab.iot.pt.server.api.model.AgencyDetails;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class AgencyDetailsService {

    private final AgencyRepository agencyRepository;


    @PreAgencyAuthorize
    public AgencyDetails getAgency(String instance) {

        AgencyEntity agencyEntity = agencyRepository.findByAgencyCodeEquals(instance);
        AgencyAddress agencyAddress = new AgencyAddress();
        agencyAddress.street(agencyEntity.getStreet());
        agencyAddress.houseNumber(agencyEntity.getHouseNumber());
        agencyAddress.flatNumber(agencyEntity.getFlatNumber());
        agencyAddress.postalCode(agencyEntity.getPostalCode());
        agencyAddress.postalCity(agencyEntity.getPostalCity());
        agencyAddress.lon(agencyEntity.getLongitude().floatValue());
        agencyAddress.lat(agencyEntity.getLatitude().floatValue());

        return new AgencyDetails()
                .agencyName(agencyEntity.getAgencyName())
                .agencyUrl(agencyEntity.getAgencyUrl())
                .agencyTimetableUrl(agencyEntity.getAgencyTimetableUrl())
                .agencyAddress(agencyAddress);
    }
}
