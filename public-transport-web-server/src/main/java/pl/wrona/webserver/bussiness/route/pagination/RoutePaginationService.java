package pl.wrona.webserver.bussiness.route.pagination;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Routes;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.mapper.RouteMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;

@Service
@AllArgsConstructor
public class RoutePaginationService {

    private final RouteQueryService routeQueryService;
    private final StopService stopService;

    @PreAgencyAuthorize
    public Routes getRoutes(String instance) {
        var routes = routeQueryService.findByAgencyCode(instance);

        var stopsIds = routes.stream()
                .map(routeEntity -> List.of(routeEntity.getOriginStopId(), routeEntity.getDestinationStopId()))
                .flatMap(List::stream)
                .toList();

        var routesWithBrigades = routeQueryService.findByExistsBrigade(instance);

        var items = routes.stream()
                .map(route -> RouteMapper.map(route, stopService.mapStopByIdsIn(stopsIds), routesWithBrigades))
                .toList();

        return new Routes()
                .items(items);
    }

}
