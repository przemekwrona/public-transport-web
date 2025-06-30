package pl.wrona.webserver.core;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.UpdateRouteRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.agency.RouteQueryRepository;
import pl.wrona.webserver.core.agency.RouteQueryService;

@Service
@AllArgsConstructor
public class RouteCommandService {

    private final RouteQueryService routeQueryService;
    private final RouteQueryRepository routeQueryRepository;

    @Transactional
    public Status updateRoute(UpdateRouteRequest updateRouteRequest) {
        var route = routeQueryService.findRouteByNameAndLine(updateRouteRequest.getRouteId());
        route.setLine(updateRouteRequest.getRoute().getLine());
        route.setName(updateRouteRequest.getRoute().getName());
        route.setGoogle(updateRouteRequest.getRoute().getGoogle());
        route.setActive(updateRouteRequest.getRoute().getActive());
        route.setDescription(updateRouteRequest.getRoute().getDescription());

        var savedRoute = routeQueryRepository.save(route);

        return new Status()
                .status(Status.StatusEnum.SUCCESS);
    }
}
