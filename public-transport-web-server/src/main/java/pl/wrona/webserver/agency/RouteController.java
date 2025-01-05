package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.igeolab.iot.agency.api.RouteApi;
import org.igeolab.iot.agency.api.model.CreateRoute;
import org.igeolab.iot.agency.api.model.CreateRouteResponse;
import org.igeolab.iot.agency.api.model.GetRouteResponse;
import org.igeolab.iot.pt.server.api.model.Route;
import org.igeolab.iot.pt.server.api.model.Routes;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class RouteController implements RouteApi, org.igeolab.iot.pt.server.api.RouteApi {

    private final RouteService routeService;

    @Override
    public ResponseEntity<CreateRouteResponse> createRoute(String agencyCode, CreateRoute createRoute) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.createRoute(agencyCode, createRoute));
    }

    @Override
    public ResponseEntity<GetRouteResponse> findRouteByLine(String agencyCode, String routeId) {
        return ResponseEntity.ok(routeService.findRouteByLine(agencyCode, routeId));
    }

    @Override
    public ResponseEntity<Status> createRoute(Route route) {
        return ResponseEntity.accepted().body(routeService.createRoute(route));
    }

    @Override
    public ResponseEntity<Routes> getRoutes() {
        return ResponseEntity.ok(routeService.getRoutes());
    }
}
