package pl.wrona.webserver.agency.route;

import lombok.AllArgsConstructor;
import org.igeolab.iot.agency.api.RouteApi;
import org.igeolab.iot.agency.api.model.CreateRoute;
import org.igeolab.iot.agency.api.model.CreateRouteResponse;
import org.igeolab.iot.agency.api.model.GetRouteResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class RouteController implements RouteApi {

    private final RouteService routeService;

    @Override
    public ResponseEntity<CreateRouteResponse> createRoute(String agencyCode, CreateRoute createRoute) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.createRoute(agencyCode, createRoute));
    }

    @Override
    public ResponseEntity<GetRouteResponse> findRouteByLine(String agencyCode, String routeId) {
        return ResponseEntity.ok(routeService.findRouteByLine(agencyCode, routeId));
    }

}
