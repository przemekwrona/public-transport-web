package pl.wrona.webserver.bussiness.timetable.generator.routes;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorFilterByRoutes;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorFilterByRoutesResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.core.agency.RouteEntity;

@Service
@AllArgsConstructor
public class TimetableRoutePaginationService {

    private final RouteQueryService routeQueryService;

    @Transactional
    public TimetableGeneratorFilterByRoutesResponse findRouteByFilter(String instance) {
        var routes = this.routeQueryService.findByAgencyCode(instance);

        var frontRoutes = routeQueryService.findRouteByExistsTripVariantFront(instance);
        var backRoutes = routeQueryService.findRouteByExistsTripVariantBack(instance);

        return new TimetableGeneratorFilterByRoutesResponse()
                .page(0)
                .items(routes.stream()
                        .map((RouteEntity route) -> new TimetableGeneratorFilterByRoutes()
                                .routeId(new RouteId()
                                        .line(route.getLine())
                                        .name(route.getName())
                                        .version(route.getVersion()))
                                .hasMainFrontVariant(frontRoutes.contains(route))
                                .hasMainBackVariant(backRoutes.contains(route)))
                        .toList());
    }
}
