package pl.wrona.webserver.core;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.RouteApi;
import org.igeolab.iot.pt.server.api.model.Route;
import org.igeolab.iot.pt.server.api.model.Routes;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.UpdateRouteRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class RouteController implements RouteApi {

    private final RouteService routeService;
    private final RouteCommandService routeCommandService;

    @Override
    public ResponseEntity<Status> createRoute(String agency, Route route) {
        return ResponseEntity.accepted().body(routeService.createRoute(route));
    }

    @Override
    public ResponseEntity<Routes> getRoutes(String agency) {
        return ResponseEntity.ok(routeService.getRoutes());
    }

    @Override
    public ResponseEntity<Status> updateRoute(String agency, UpdateRouteRequest updateRouteRequest) {
        return ResponseEntity.ok(routeCommandService.updateRoute(updateRouteRequest));
    }
}
