package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Routes;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.agency.entity.Route;
import pl.wrona.webserver.security.AppUser;

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

        Route unsavedRoute = new Route();
        unsavedRoute.setName(route.getName());
        unsavedRoute.setLine(route.getLine());
        unsavedRoute.setOrigin(route.getOrigin());
        unsavedRoute.setDestination(route.getDestination());
        unsavedRoute.setVia(route.getVia());
        unsavedRoute.setGoogle(route.getGoogle());
        unsavedRoute.setActive(route.getActive());
        unsavedRoute.setAgency(agencyService.findAgencyByAppUser(appUser));

        routeRepository.save(unsavedRoute);

        return new Status().status("CREATED");
    }

    public Routes getRoutes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        var routes = routeRepository.findAllByAgency(agencyService.findAgencyByAppUser(appUser));
        var items = routes.stream()
                .map(route -> new org.igeolab.iot.pt.server.api.model.Route()
                        .name(route.getName())
                        .line(route.getLine())
                        .google(route.isGoogle())
                        .active(route.isActive())
                        .origin(route.getOrigin())
                        .destination(route.getDestination())
                        .via(route.getVia()))
                .toList();

        return new Routes()
                .items(items);
    }

}
