package pl.wrona.webserver.bussiness.agency.updater;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.igeolab.iot.pt.server.api.model.AgencyAddress;
import org.igeolab.iot.pt.server.api.model.AgencyDetails;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.client.geoapify.GeoapifyService;
import pl.wrona.webserver.client.geoapify.geocode.GeoResponse;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AgencyUpdaterService {

    private final AgencyRepository agencyRepository;
    private final GeoapifyService geoapifyService;

    @Transactional
    @PreAgencyAuthorize
    public Status updateAgency(String instance, AgencyDetails agencyDetails) {
        AgencyEntity agencyEntity = agencyRepository.findByAgencyCodeEquals(instance);
        agencyEntity.setAgencyName(agencyDetails.getAgencyName());
        agencyEntity.setAgencyUrl(agencyDetails.getAgencyUrl());
        agencyEntity.setAgencyTimetableUrl(agencyDetails.getAgencyTimetableUrl());

        Optional.ofNullable(agencyDetails.getAgencyAddress()).ifPresent(agencyAddress -> {
            agencyEntity.setStreet(agencyAddress.getStreet());
            agencyEntity.setFlatNumber(agencyAddress.getFlatNumber());
            agencyEntity.setHouseNumber(agencyAddress.getHouseNumber());
            agencyEntity.setPostalCode(agencyAddress.getPostalCode());
            agencyEntity.setPostalCity(agencyAddress.getPostalCity());

            String formatedAddress = getFormattedAddress(agencyAddress);
            GeoResponse addressResponse = geoapifyService.geoapify(formatedAddress);
            addressResponse.features().stream().findFirst().ifPresent(feature -> {
                agencyEntity.setLongitude(feature.geometry().coordinates().get(0));
                agencyEntity.setLatitude(feature.geometry().coordinates().get(1));
            });
        });

        agencyRepository.save(agencyEntity);

        return new Status()
                .status(Status.StatusEnum.SUCCESS);
    }

    private static String getFormattedAddress(AgencyAddress agencyAddress) {
        if (agencyAddress.getFlatNumber().equals(StringUtils.EMPTY)) {
            return "%s %s; %s %s".formatted(agencyAddress.getStreet(), agencyAddress.getHouseNumber(), agencyAddress.getPostalCode(), agencyAddress.getPostalCity());
        } else {
            return "%s %s/%s; %s %s".formatted(agencyAddress.getStreet(), agencyAddress.getHouseNumber(), agencyAddress.getFlatNumber(), agencyAddress.getPostalCode(), agencyAddress.getPostalCity());
        }
    }
}
