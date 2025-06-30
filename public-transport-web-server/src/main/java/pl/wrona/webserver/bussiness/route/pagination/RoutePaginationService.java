package pl.wrona.webserver.bussiness.route.pagination;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Routes;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.agency.RouteQueryRepository;
import pl.wrona.webserver.core.mapper.RouteMapper;

import java.util.List;

@Service
@AllArgsConstructor
public class RoutePaginationService {

    private final RouteQueryRepository routeQueryRepository;
    private final StopService stopService;

    public Routes getRoutes(String instance) {
        var routes = routeQueryRepository.findByAgencyCode(instance);

        var stopsIds = routes.stream()
                .map(routeEntity -> List.of(routeEntity.getOriginStopId(), routeEntity.getDestinationStopId()))
                .flatMap(List::stream)
                .toList();

        var items = routes.stream()
                .map(route -> RouteMapper.map(route, stopService.mapStopByIdsIn(stopsIds)))
                .toList();

        return new Routes()
                .items(items);
    }

}
