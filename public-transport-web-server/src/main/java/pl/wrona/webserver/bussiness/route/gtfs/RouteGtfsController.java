package pl.wrona.webserver.bussiness.route.gtfs;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.RouteGtfsApi;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class RouteGtfsController implements RouteGtfsApi {

    private final RouteGtfsService routeGtfsService;

    @Override
    public ResponseEntity<Resource> downloadRouteGtfs(String agency, String routeCode) {
        return routeGtfsService.downloadRouteGtfs(agency, routeCode);
    }
}
