package pl.wrona.webserver.bussiness.route.stop.sequencer;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteStopGraph;
import org.igeolab.iot.pt.server.api.model.RouteStopGraphNode;
import org.igeolab.iot.pt.server.api.model.RouteStops;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.bussiness.stop.name.AgencyStopNameService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class RouteStopsService {

    private final RouteQueryService routeQueryService;
    private final TripQueryService tripQueryService;
    private final StopGraphSequencerService stopGraphSequencerService;
    private final AgencyStopNameService agencyStopNameService;

    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER,
            PreAgencyAuthorize.Role.BRIGADIER,
            PreAgencyAuthorize.Role.DISPATCHER,
            PreAgencyAuthorize.Role.DRIVER
    })
    public RouteStops getRouteStops(String instance, String routeCode) {
        RouteEntity route = routeQueryService.findRouteByAgencyCodeAndRouteCode(instance, routeCode);
        List<TripEntity> trips = tripQueryService.findByAgencyCodeAndRoute(instance, route);
        return new RouteStops()
                .front(applyAgencyNames(instance, RouteStopGraphMapper.map(stopGraphSequencerService.sequence(trips, TripVariantMode.FRONT))))
                .back(applyAgencyNames(instance, RouteStopGraphMapper.map(stopGraphSequencerService.sequence(trips, TripVariantMode.BACK))));
    }

    private RouteStopGraph applyAgencyNames(String instance, RouteStopGraph graph) {
        if (graph == null || graph.getNodes() == null || graph.getNodes().isEmpty()) {
            return graph;
        }
        Map<Long, String> agencyNames = agencyStopNameService.findOverrides(instance, graph.getNodes().stream().map(RouteStopGraphNode::getStopId).toList());
        graph.getNodes().forEach(node -> node.setName(AgencyStopNameService.prefer(node.getName(), agencyNames, node.getStopId())));
        return graph;
    }
}
