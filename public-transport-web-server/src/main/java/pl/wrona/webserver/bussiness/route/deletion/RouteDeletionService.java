package pl.wrona.webserver.bussiness.route.deletion;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.ModificationRouteResponse;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.route.RouteCommandService;
import pl.wrona.webserver.bussiness.trip.TripCommandService;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class RouteDeletionService {

    private final RouteQueryService routeQueryService;
    private final TripCommandService tripCommandService;
    private final RouteCommandService routeCommandService;

    @Transactional
    @PreAgencyAuthorize
    public ModificationRouteResponse deleteRoute(String instance, RouteId routeId) {
        RouteEntity preDeletedRoute = routeQueryService.findRouteByAgencyCodeAndRouteId(instance, routeId);
        tripCommandService.deleteTripByRoute(preDeletedRoute);
        routeCommandService.deleteByRoute(preDeletedRoute);

        return new ModificationRouteResponse()
                .status(new Status().status(Status.StatusEnum.DELETED))
                .routeId(routeId);
    }
}
