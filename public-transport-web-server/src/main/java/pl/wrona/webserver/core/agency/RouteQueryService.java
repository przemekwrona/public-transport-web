package pl.wrona.webserver.core.agency;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.entity.Agency;

@Service
@AllArgsConstructor
public class RouteQueryService {

    private final AgencyService agencyService;
    private final RouteQueryRepository routeQueryRepository;

    @Transactional
    public RouteEntity findRouteByNameAndLine(String name, String line) {
        Agency agency = this.agencyService.getLoggedAgency();
        return this.routeQueryRepository.findByAgencyCodeAndRouteId(agency, name, line);
    }

    @Transactional
    public RouteEntity findRouteByNameAndLine(RouteId routeId) {
        return findRouteByNameAndLine(routeId.getName(), routeId.getLine());
    }

}
