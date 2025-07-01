package pl.wrona.webserver.bussiness.route.deletion;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.ModificationRouteResponse;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.RouteQueryService;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class RouteDeletionService {

    private final RouteQueryService routeQueryService;
    private final RouteDeletionRepository routeDeletionRepository;

    @PreAgencyAuthorize
    public ModificationRouteResponse deleteRoute(String instance, RouteId routeId) {
        RouteEntity preDeletedRoute = routeQueryService.findRouteByAgencyCodeAndLineAndName(instance, routeId.getLine(), routeId.getName());
        routeDeletionRepository.delete(preDeletedRoute);

        return new ModificationRouteResponse()
                .status(new Status().status(Status.StatusEnum.DELETED))
                .routeId(routeId);
    }
}
