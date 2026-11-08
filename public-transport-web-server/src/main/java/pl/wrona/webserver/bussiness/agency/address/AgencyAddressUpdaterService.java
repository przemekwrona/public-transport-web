package pl.wrona.webserver.bussiness.agency.address;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.AgencyAddress;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.client.geoapify.GeoapifyService;
import pl.wrona.webserver.client.geoapify.geocode.Feature;
import pl.wrona.webserver.client.geoapify.geocode.GeoResponse;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;

@Service
@AllArgsConstructor
public class AgencyAddressUpdaterService {

    private final AgencyRepository agencyRepository;
    private final GeoapifyService geoapifyService;

    @Transactional
    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.AGENCY_ADMIN
    })
    public Status updateAgencyAddress(String instance, AgencyAddress agencyAddress) {
        AgencyEntity agencyEntity = agencyRepository.findByAgencyCodeEquals(instance);
        apply(agencyEntity, agencyAddress);
        agencyRepository.save(agencyEntity);
        return new Status().status(Status.StatusEnum.SUCCESS);
    }

    public void apply(AgencyEntity agencyEntity, AgencyAddress agencyAddress) {
        agencyEntity.setStreet(agencyAddress.getStreet());
        agencyEntity.setHouseNumber(agencyAddress.getHouseNumber());
        agencyEntity.setFlatNumber(agencyAddress.getFlatNumber());
        agencyEntity.setPostalCode(agencyAddress.getPostalCode());
        agencyEntity.setPostalCity(agencyAddress.getPostalCity());

        GeoResponse addressResponse = geoapifyService.geoapify(formattedAddress(agencyAddress));
        if (addressResponse == null || addressResponse.features() == null) {
            return;
        }
        addressResponse.features().stream().findFirst().ifPresent(feature -> applyCoordinates(agencyEntity, feature));
    }

    private static void applyCoordinates(AgencyEntity agencyEntity, Feature feature) {
        if (feature.geometry() == null || feature.geometry().coordinates() == null) {
            return;
        }
        List<Double> coordinates = feature.geometry().coordinates();
        if (coordinates.size() < 2) {
            return;
        }
        agencyEntity.setLongitude(coordinates.get(0));
        agencyEntity.setLatitude(coordinates.get(1));
    }

    static String formattedAddress(AgencyAddress agencyAddress) {
        String flatNumber = agencyAddress.getFlatNumber();
        if (flatNumber == null || flatNumber.isBlank()) {
            return "%s %s; %s %s".formatted(
                    agencyAddress.getStreet(),
                    agencyAddress.getHouseNumber(),
                    agencyAddress.getPostalCode(),
                    agencyAddress.getPostalCity());
        }
        return "%s %s/%s; %s %s".formatted(
                agencyAddress.getStreet(),
                agencyAddress.getHouseNumber(),
                flatNumber,
                agencyAddress.getPostalCode(),
                agencyAddress.getPostalCity());
    }
}
