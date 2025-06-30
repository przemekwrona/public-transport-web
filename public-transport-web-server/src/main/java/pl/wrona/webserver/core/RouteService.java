package pl.wrona.webserver.core;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Route;
import org.igeolab.iot.pt.server.api.model.RouteDetails;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Routes;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.route.creator.RouteCreatorService;
import pl.wrona.webserver.bussiness.route.pagination.RoutePaginationService;
import pl.wrona.webserver.bussiness.trip.reader.route.TripReaderByRouteService;
import pl.wrona.webserver.core.agency.RouteQueryRepository;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.security.AppUser;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;

@Service
@AllArgsConstructor
public class RouteService {

    private final RouteQueryRepository routeQueryRepository;
    private final AgencyService agencyService;

    private final RouteCreatorService routeCreatorService;
    private final RoutePaginationService routePaginationService;
    private final TripReaderByRouteService tripReaderByRouteService;

    public List<RouteEntity> getRoutesEntities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        return routeQueryRepository.findAllByAgencyOrderByLineAscNameAsc(agencyService.findAgencyByAppUser(appUser));
    }

    @PreAgencyAuthorize
    public Status createRoute(String instance, Route route) {
        return routeCreatorService.createRoute(instance, route);
    }

    @PreAgencyAuthorize
    public Routes getRoutes(String instance) {
        return routePaginationService.getRoutes(instance);
    }

    @PreAgencyAuthorize
    public RouteDetails getRouteDetails(String instance, RouteId routeId) {
        return tripReaderByRouteService.getRouteDetails(instance, routeId);
    }

}
