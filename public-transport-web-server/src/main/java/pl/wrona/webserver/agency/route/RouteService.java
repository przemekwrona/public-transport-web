package pl.wrona.webserver.agency.route;

import lombok.AllArgsConstructor;
import org.igeolab.iot.agency.api.model.CreateRoute;
import org.igeolab.iot.agency.api.model.CreateRouteResponse;
import org.igeolab.iot.agency.api.model.CreateRouteStopTime;
import org.igeolab.iot.agency.api.model.CreateRouteTrip;
import org.igeolab.iot.agency.api.model.GetRouteResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.agency.agency.AgencyService;
import pl.wrona.webserver.agency.model.Agency;
import pl.wrona.webserver.agency.model.Route;
import pl.wrona.webserver.agency.model.Stop;
import pl.wrona.webserver.agency.model.Trip;
import pl.wrona.webserver.agency.stop.StopService;
import pl.wrona.webserver.agency.trip.TripService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final TripService tripService;
    private final AgencyService agencyService;
    private final StopService stopService;

    @Transactional
    public CreateRouteResponse createRoute(String agencyCode, CreateRoute createRoute) {
        Agency agency = agencyService.findAgencyByAgencyCode(agencyCode);

        List<CreateRouteStopTime> notSavedStops = createRoute.getTrips().stream()
                .map(CreateRouteTrip::getStops)
                .flatMap(Collection::stream)
                .filter(stop -> stop.getOtpId() == null)
                .toList();

        List<Long> savedIds = stopService.save(notSavedStops);

        List<Long> stopIds = createRoute.getTrips().stream()
                .map(CreateRouteTrip::getStops)
                .flatMap(Collection::stream)
                .map(CreateRouteStopTime::getOtpId)
                .toList();

        Map<Long, Stop> stopDictionary = stopService.mapStopByIdsIn(Stream.concat(savedIds.stream(), stopIds.stream()).toList());

        Route route = new Route();
        route.setAgency(agency);
        route.setRouteId(createRoute.getLine());

        Route savedRoute = routeRepository.save(route);

        Set<Trip> trips = createRoute.getTrips().stream()
                .map(trip -> Trip.builder()
                        .tripId("%s/%s/%s".formatted(agency.getAgencyCode(), createRoute.getLine(), "1"))
                        .route(savedRoute)
                        .build())
                .collect(Collectors.toSet());

        List<Trip> savedTrips = tripService.save(trips);

        return new CreateRouteResponse().id("NEXTBUS/%s".formatted(createRoute.getLine()));
    }

    @Transactional
    public GetRouteResponse findRouteByLine(String agencyCode, String routeId) {
        Route route = routeRepository.findByAgencyCodeAndRouteId(agencyCode, routeId);

        if (route == null) {
            return new GetRouteResponse();
        }

        GetRouteResponse response = new GetRouteResponse()
                .agencyCode(route.getAgency().getAgencyCode())
                .line(route.getRouteId());
//                .trips(route.getTrips().stream()
//                        .map(trip -> new GetRouteTrip()
//                                .stops(List.of()))
//                        .toList());

        return response;
    }

}
