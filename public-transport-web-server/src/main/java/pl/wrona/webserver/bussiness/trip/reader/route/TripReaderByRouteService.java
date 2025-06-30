package pl.wrona.webserver.bussiness.trip.reader.route;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteDetails;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.agency.RouteQueryService;
import pl.wrona.webserver.core.agency.TripQueryService;
import pl.wrona.webserver.core.mapper.RouteMapper;
import pl.wrona.webserver.core.mapper.TripMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;

@Service
@AllArgsConstructor
public class TripReaderByRouteService {

    private final RouteQueryService routeQueryService;
    private final TripQueryService tripQueryService;
    private final StopService stopService;

    @PreAgencyAuthorize
    public RouteDetails getRouteDetails(String instance, RouteId routeId) {
        var route = routeQueryService.findRouteByAgencyCodeAndLineAndName(instance, routeId.getLine(), routeId.getName());
        var trips = tripQueryService.findByAgencyCodeAndLineAndName(instance, routeId.getLine(), routeId.getName()).stream()
                .map(TripMapper::map)
                .toList();

        var stopsIds = List.of(route.getOriginStopId(), route.getDestinationStopId());
        var stopDictionary = stopService.mapStopByIdsIn(stopsIds);

        return new RouteDetails()
                .route(RouteMapper.map(route, stopDictionary))
                .trips(trips);
    }
}
