package pl.wrona.webserver.core;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Routes;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.route.pagination.RoutePaginationService;
import pl.wrona.webserver.core.agency.RouteQueryRepository;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.mapper.RouteMapper;
import pl.wrona.webserver.security.AppUser;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;

@Service
@AllArgsConstructor
public class RouteService {

    private final RouteQueryRepository routeQueryRepository;
    private final TripService tripService;
    private final AgencyService agencyService;
    private final StopService stopService;
    private final RoutePaginationService routePaginationService;

    @Transactional
    public Status createRoute(org.igeolab.iot.pt.server.api.model.Route route) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        RouteEntity unsavedRouteEntity = new RouteEntity();
        unsavedRouteEntity.setName(route.getName());
        unsavedRouteEntity.setLine(route.getLine());

        unsavedRouteEntity.setOriginStopId(route.getOriginStop().getId());
        unsavedRouteEntity.setOriginStopName(stopService.findStopByIdsIn(List.of(route.getOriginStop().getId())).get(0).getName());

        unsavedRouteEntity.setDestinationStopId(route.getDestinationStop().getId());
        unsavedRouteEntity.setDestinationStopName(stopService.findStopByIdsIn(List.of(route.getDestinationStop().getId())).get(0).getName());

        unsavedRouteEntity.setVia(route.getVia());
        unsavedRouteEntity.setGoogle(route.getGoogle());
        unsavedRouteEntity.setActive(route.getActive());
        unsavedRouteEntity.setAgency(agencyService.findAgencyByAppUser(appUser));

        routeQueryRepository.save(unsavedRouteEntity);

        return new Status().status(Status.StatusEnum.CREATED);
    }

    public List<RouteEntity> getRoutesEntities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        return routeQueryRepository.findAllByAgencyOrderByLineAscNameAsc(agencyService.findAgencyByAppUser(appUser));
    }

    @PreAgencyAuthorize
    public Routes getRoutes(String instance) {
        return routePaginationService.getRoutes(instance);
    }

}
