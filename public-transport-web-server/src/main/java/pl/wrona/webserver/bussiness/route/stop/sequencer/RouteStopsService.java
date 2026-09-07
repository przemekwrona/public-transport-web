package pl.wrona.webserver.bussiness.route.stop.sequencer;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteStops;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;

@Service
@AllArgsConstructor
public class RouteStopsService {

    private final RouteQueryService routeQueryService;
    private final TripQueryService tripQueryService;
    private final StopGraphSequencerService stopGraphSequencerService;

    @PreAgencyAuthorize
    public RouteStops getRouteStops(String instance, String routeCode) {
        RouteEntity route = routeQueryService.findRouteByAgencyCodeAndRouteCode(instance, routeCode);
        List<TripEntity> trips = tripQueryService.findByAgencyCodeAndRoute(instance, route);
        return new RouteStops()
                .front(RouteStopGraphMapper.map(stopGraphSequencerService.sequence(trips, TripVariantMode.FRONT)))
                .back(RouteStopGraphMapper.map(stopGraphSequencerService.sequence(trips, TripVariantMode.BACK)));
    }
}
