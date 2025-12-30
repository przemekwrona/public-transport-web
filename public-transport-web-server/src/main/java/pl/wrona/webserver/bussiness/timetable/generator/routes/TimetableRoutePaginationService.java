package pl.wrona.webserver.bussiness.timetable.generator.routes;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorFilterByRoutes;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorFilterByRoutesResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.route.RouteQueryService;

@Service
@AllArgsConstructor
public class TimetableRoutePaginationService {

    private final RouteQueryService routeQueryService;

    public TimetableGeneratorFilterByRoutesResponse findRouteByFilter(String instance) {
        var routes = this.routeQueryService.findByAgencyCode(instance);

        return new TimetableGeneratorFilterByRoutesResponse()
                .page(0)
                .items(routes.stream()
                        .map(route -> new TimetableGeneratorFilterByRoutes()
                                .line(route.getLine())
                                .name(route.getName()))
                        .toList());
    }
}
