package pl.wrona.webserver.core;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Routes;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.agency.RouteQueryRepository;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.mapper.RouteMapper;
import pl.wrona.webserver.security.AppUser;

import java.util.List;

@Service
@AllArgsConstructor
public class RouteService {

    private final RouteQueryRepository routeQueryRepository;
    private final TripService tripService;
    private final AgencyService agencyService;
    private final StopService stopService;

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
        unsavedRouteEntity.setAgencyEntity(agencyService.findAgencyByAppUser(appUser));

        routeQueryRepository.save(unsavedRouteEntity);

        return new Status().status(Status.StatusEnum.CREATED);
    }

    public List<RouteEntity> getRoutesEntities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        return routeQueryRepository.findAllByAgencyOrderByLineAscNameAsc(agencyService.findAgencyByAppUser(appUser));
    }

    public Routes getRoutes() {
        var routes = getRoutesEntities();
        var stopsIds = routes.stream()
                .map(routeEntity -> List.of(routeEntity.getOriginStopId(), routeEntity.getDestinationStopId()))
                .flatMap(List::stream)
                .toList();


        var items = routes.stream()
                .map(route -> RouteMapper.map(route, stopService.mapStopByIdsIn(stopsIds)))
                .toList();

        return new Routes().items(items);
    }

    public RouteEntity getRouteByLineAndName(String line, String name) {
        return this.routeQueryRepository.findByAgencyCodeAndRouteId(agencyService.getLoggedAgency(), line, name);
    }
}
