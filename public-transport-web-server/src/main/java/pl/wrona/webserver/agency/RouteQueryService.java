package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.agency.entity.Agency;
import pl.wrona.webserver.agency.entity.Route;
import pl.wrona.webserver.security.AppUser;

@Service
@AllArgsConstructor
public class RouteQueryService {

    private final RouteRepository routeRepository;
    private final AgencyRepository agencyRepository;


    @Transactional
    public Route findRouteByNameAndLine(String name, String line) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        Agency agency = this.agencyRepository.findByAppUser(appUser);
        return this.routeRepository.findByAgencyCodeAndRouteId(agency, name, line);
    }

    @Transactional
    public Route findRouteByNameAndLine(RouteId routeId) {
        return findRouteByNameAndLine(routeId.getName(), routeId.getLine());
    }

}
