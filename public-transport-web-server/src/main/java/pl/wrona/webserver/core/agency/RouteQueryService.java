package pl.wrona.webserver.core.agency;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.AgencyService;

@Service
@AllArgsConstructor
public class RouteQueryService {

    private final AgencyService agencyService;
    private final RouteQueryRepository routeQueryRepository;

    @Transactional
    public RouteEntity findRouteByNameAndLine(String name, String line) {
        AgencyEntity agencyEntity = this.agencyService.getLoggedAgency();
        return this.routeQueryRepository.findByAgencyCodeAndRouteId(agencyEntity, name, line);
    }

    @Transactional
    public RouteEntity findRouteByNameAndLine(RouteId routeId) {
        return findRouteByNameAndLine(routeId.getName(), routeId.getLine());
    }

}
