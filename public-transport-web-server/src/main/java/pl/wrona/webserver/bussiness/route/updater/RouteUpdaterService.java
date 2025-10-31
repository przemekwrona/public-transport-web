package pl.wrona.webserver.bussiness.route.updater;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.UpdateRouteRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.route.LineNameCleaner;
import pl.wrona.webserver.bussiness.route.RouteCommandService;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class RouteUpdaterService {

    private final RouteQueryService routeQueryService;
    private final RouteCommandService routeCommandService;

    @Transactional
    @PreAgencyAuthorize
    public Status updateRoute(String instance, UpdateRouteRequest updateRouteRequest) {
        var route = routeQueryService.findRouteByAgencyCodeAndRouteId(instance, updateRouteRequest.getRouteId());
        route.setLine(updateRouteRequest.getRoute().getLine());
        route.setName(LineNameCleaner.clean(updateRouteRequest.getRoute().getName()));
        route.setGoogle(updateRouteRequest.getRoute().getGoogle());
        route.setActive(updateRouteRequest.getRoute().getActive());
        route.setDescription(updateRouteRequest.getRoute().getDescription());
        route.setUpdatedAt(LocalDateTime.now());

        var savedRoute = routeCommandService.updateRoute(route);

        return new Status()
                .status(Status.StatusEnum.SUCCESS);
    }
}
