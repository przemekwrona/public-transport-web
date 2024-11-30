package pl.wrona.webserver.agency.route;

import lombok.AllArgsConstructor;
import org.igeolab.iot.agency.api.model.CreateRoute;
import org.igeolab.iot.agency.api.model.CreateRouteResponse;
import org.igeolab.iot.agency.api.model.CreateRouteStopTime;
import org.igeolab.iot.agency.api.model.CreateRouteTrip;
import org.igeolab.iot.agency.api.model.GetRouteResponse;
import org.igeolab.iot.gtfs.server.api.model.Trips;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.agency.agency.AgencyService;
import pl.wrona.webserver.agency.model.Agency;
import pl.wrona.webserver.agency.model.Route;
import pl.wrona.webserver.agency.model.RouteId;
import pl.wrona.webserver.agency.model.Stop;
import pl.wrona.webserver.agency.stop.StopService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
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

        RouteId routeId = new RouteId();
        routeId.setRouteId(createRoute.getLine());
        routeId.setAgency(agency);

        Route route = new Route();
        route.setRouteId(routeId);

        routeRepository.save(route);

        return new CreateRouteResponse().id("NEXTBUS/%s".formatted(createRoute.getLine()));
    }

    public GetRouteResponse findRouteByLine(String agencyCode, String routeId) {
        return new GetRouteResponse()
                .line(routeId);
    }

}
