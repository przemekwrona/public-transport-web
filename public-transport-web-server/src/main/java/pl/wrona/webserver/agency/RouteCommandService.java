package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.UpdateRouteRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class RouteCommandService {

    private final RouteQueryService routeQueryService;
    private final RouteRepository routeRepository;

    @Transactional
    public Status updateRoute(UpdateRouteRequest updateRouteRequest) {
        var route = routeQueryService.findRouteByNameAndLine(updateRouteRequest.getRouteId());
        route.setLine(updateRouteRequest.getRoute().getLine());
        route.setName(updateRouteRequest.getRoute().getName());
        route.setGoogle(updateRouteRequest.getRoute().getGoogle());
        route.setActive(updateRouteRequest.getRoute().getActive());
        route.setDescription(updateRouteRequest.getRoute().getDescription());

        var savedRoute = routeRepository.save(route);

        return new Status()
                .status("OK");
    }
}
