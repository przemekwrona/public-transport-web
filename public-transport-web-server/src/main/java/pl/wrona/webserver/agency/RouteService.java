package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Routes;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.agency.entity.RouteEntity;
import pl.wrona.webserver.agency.mapper.RouteMapper;
import pl.wrona.webserver.security.AppUser;

import java.util.List;

@Service
@AllArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
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
        unsavedRouteEntity.setAgency(agencyService.findAgencyByAppUser(appUser));

        routeRepository.save(unsavedRouteEntity);

        return new Status().status(Status.StatusEnum.CREATED);
    }

    public List<RouteEntity> getRoutesEntities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        return routeRepository.findAllByAgencyOrderByLineAscNameAsc(agencyService.findAgencyByAppUser(appUser));
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

}
