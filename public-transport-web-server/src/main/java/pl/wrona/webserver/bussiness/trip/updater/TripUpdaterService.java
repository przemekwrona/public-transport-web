package pl.wrona.webserver.bussiness.trip.updater;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.UpdateTripDetailsRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.trip.TripCommandService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.core.mapper.TripMapper;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class TripUpdaterService {

    private final TripQueryService tripQueryService;
    private final TripCommandService tripCommandService;

    @Transactional
    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER
    })
    public Status updateTrip(String instance, String routeCode, String tripCode, UpdateTripDetailsRequest updateTripDetailsRequest) {
        var tripDetails = updateTripDetailsRequest.getBody();
        TripEntity tripEntity = tripQueryService.findTripByAgencyAndRouteCodeAndTripCode(instance, routeCode, tripCode);

        boolean mainVariant = Boolean.TRUE.equals(tripDetails.getIsMainVariant());
        String designation = mainVariant ? StringUtils.EMPTY : StringUtils.trimToEmpty(tripDetails.getVariantDesignation());
        TripVariantMode tripVariantMode = TripVariantModeMapper.map(updateTripDetailsRequest.getBody().getTripId().getVariantMode());

        if (tripEntity != null
                && !tripEntity.getRoute().getRouteCode().equals(routeCode)
                && !tripEntity.getVariantMode().equals(tripVariantMode)
                && !tripEntity.getVariantDesignation().equals(designation)) {
            tripQueryService.assertUniqueTrip(instance, routeCode, mainVariant, designation, tripVariantMode, tripEntity.getTripId());
        }

        TripEntity updatedTrip = TripMapper.update(tripEntity, tripDetails);
        tripCommandService.save(updatedTrip);

        return new Status()
                .status(Status.StatusEnum.SUCCESS);
    }

}
