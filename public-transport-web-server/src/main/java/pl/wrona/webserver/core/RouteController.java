package pl.wrona.webserver.core;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.RouteApi;
import org.igeolab.iot.pt.server.api.model.Route;
import org.igeolab.iot.pt.server.api.model.RouteDetails;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Routes;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.UpdateRouteRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.route.creator.RouteCreatorService;
import pl.wrona.webserver.bussiness.route.pagination.RoutePaginationService;
import pl.wrona.webserver.bussiness.route.updater.RouteUpdaterService;
import pl.wrona.webserver.bussiness.trip.reader.route.TripReaderByRouteService;

@RestController
@AllArgsConstructor
public class RouteController implements RouteApi {

    private final RouteCreatorService routeCreatorService;
    private final RoutePaginationService routePaginationService;
    private final TripReaderByRouteService tripReaderByRouteService;
    private final RouteUpdaterService routeUpdaterService;

    @Override
    public ResponseEntity<Status> createRoute(String agency, Route route) {
        return ResponseEntity.accepted().body(routeCreatorService.createRoute(agency, route));
    }

    @Override
    public ResponseEntity<RouteDetails> getRouteDetails(String agency, RouteId routeId) {
        return ResponseEntity.ok(tripReaderByRouteService.getRouteDetails(agency, routeId));
    }

    @Override
    public ResponseEntity<Routes> getRoutes(String agency) {
        return ResponseEntity.ok(routePaginationService.getRoutes(agency));
    }

    @Override
    public ResponseEntity<Status> updateRoute(String agency, UpdateRouteRequest updateRouteRequest) {
        return ResponseEntity.ok(routeUpdaterService.updateRoute(agency, updateRouteRequest));
    }
}
