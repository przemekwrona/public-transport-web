package pl.wrona.webserver.core.agency;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.AgencyService;

import java.util.List;

@Service
@AllArgsConstructor
public class RouteQueryService {

    private final AgencyService agencyService;
    private final RouteQueryRepository routeQueryRepository;

    public RouteEntity findRouteByAgencyCodeAndLineAndName(String agencyCode, String line, String name) {
        return this.routeQueryRepository.findByAgencyCodeAndLineAndName(agencyCode, line, name);
    }

    public List<RouteEntity> findRouteByAgencyCode(String agencyCode) {
        return this.routeQueryRepository.findByAgencyCode(agencyCode);
    }

    @Deprecated
    @Transactional
    public RouteEntity findRouteByNameAndLine(String name, String line) {
        AgencyEntity agencyEntity = this.agencyService.getLoggedAgency();
        return this.routeQueryRepository.findByAgencyCodeAndRouteId(agencyEntity, name, line);
    }

    @Deprecated
    @Transactional
    public RouteEntity findRouteByNameAndLine(RouteId routeId) {
        return findRouteByNameAndLine(routeId.getName(), routeId.getLine());
    }

}
