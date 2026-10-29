package pl.wrona.webserver.bussiness.trip.existance;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.TripExistence;
import org.igeolab.iot.pt.server.api.model.TripExistenceResult;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.trip.TripQueryRepository;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;

@Service
@AllArgsConstructor
public class TripExistenceService {

    private final TripQueryService tripQueryService;
    private final TripQueryRepository tripQueryRepository;

    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER
    })
    @Transactional(readOnly = true)
    public TripExistenceResult hasVariantDetails(String instance, TripExistence tripExistence) {
        String routeCode = tripExistence.getRouteId() == null ? null : tripExistence.getRouteId().getRouteCode();
        boolean mainVariant = Boolean.TRUE.equals(tripExistence.getIsMainVariant());
        String designation = tripExistence.getVariantDesignation();
        TripVariantMode variantMode = TripVariantModeMapper.map(tripExistence.getTripMode());
        Long excludedTripId = currentTripId(instance, routeCode, tripExistence.getTripCode());

        List<TripEntity> matches = tripQueryService.findByRouteMainVariantAndDesignation(
                instance, routeCode, mainVariant, designation, variantMode, excludedTripId);
        if (matches.isEmpty()) {
            return result(Status.StatusEnum.NOT_EXIST, null);
        }
        return result(Status.StatusEnum.EXISTS, toTripId(matches.get(0)));
    }

    private Long currentTripId(String instance, String routeCode, String tripCode) {
        if (StringUtils.isBlank(routeCode) || StringUtils.isBlank(tripCode)) {
            return null;
        }
        TripEntity current = tripQueryRepository.findTripByAgencyAndRouteCodeAndTripCode(instance, routeCode, tripCode);
        return current == null ? null : current.getTripId();
    }

    private TripId toTripId(TripEntity trip) {
        RouteEntity route = trip.getRoute();
        return new TripId()
                .routeId(new RouteId()
                        .line(route.getLine())
                        .name(route.getName())
                        .version(route.getVersion())
                        .routeCode(route.getRouteCode()))
                .variantMode(TripVariantModeMapper.map(trip.getVariantMode()))
                .tripCode(trip.getTripCode());
    }

    private TripExistenceResult result(Status.StatusEnum status, TripId tripId) {
        return new TripExistenceResult()
                .status(new Status().status(status))
                .tripId(tripId);
    }
}
