package pl.wrona.webserver.bussiness.route.details;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteDetails;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.core.mapper.RouteMapper;
import pl.wrona.webserver.core.mapper.TripMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class RouteDetailsService {

    private final RouteQueryService routeQueryService;
    private final TripQueryService tripQueryService;
    private final StopService stopService;

    @PreAgencyAuthorize
    public RouteDetails getRouteDetails(String instance, RouteId routeId) {
        var route = routeQueryService.findRouteByAgencyCodeAndRouteId(instance, routeId);
        var tripWithBrigades = tripQueryService.mapByExistsBrigade(instance);

        var trips = tripQueryService.findByAgencyCodeAndLineAndName(instance, routeId.getLine(), routeId.getName()).stream()
                .map(tripEntity -> TripMapper.map(tripEntity, tripWithBrigades))
                .toList();

        var stopsIds = List.of(route.getOriginStopId(), route.getDestinationStopId());
        var stopDictionary = stopService.mapStopByIdsIn(stopsIds);

        return new RouteDetails()
                .route(RouteMapper.map(route, stopDictionary, Map.of()))
                .trips(trips);
    }
}
