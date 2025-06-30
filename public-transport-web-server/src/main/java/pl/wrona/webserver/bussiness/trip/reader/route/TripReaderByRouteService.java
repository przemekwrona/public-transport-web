package pl.wrona.webserver.bussiness.trip.reader.route;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Trips;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.agency.RouteQueryService;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.mapper.RouteMapper;
import pl.wrona.webserver.core.mapper.TripMapper;

import java.util.List;

@Service
@AllArgsConstructor
public class TripReaderByRouteService {

    private final AgencyService agencyService;
    private final TripReaderByRouteRepository tripReaderByRouteRepository;
    private final RouteQueryService routeQueryService;
    private final StopService stopService;

    public Trips getTrips(RouteId routeId) {
        AgencyEntity loggedAppUser = agencyService.getLoggedAgency();

        var route = routeQueryService.findRouteByNameAndLine(routeId.getLine(), routeId.getName());

        var trips = tripReaderByRouteRepository.findAllByAgencyAndLineAndName(loggedAppUser, routeId.getLine(), routeId.getName()).stream()
                .map(TripMapper::map)
                .toList();

        var stopsIds = List.of(route.getOriginStopId(), route.getDestinationStopId());
        var stopDictionary = stopService.mapStopByIdsIn(stopsIds);

        return new Trips()
                .route(RouteMapper.map(route, stopDictionary))
                .trips(trips);
    }
}
