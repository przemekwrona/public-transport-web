package pl.wrona.webserver.bussiness.route;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.RouteEntity;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RouteQueryService {

    private final AgencyService agencyService;
    private final RouteQueryRepository routeQueryRepository;

    public List<RouteEntity> findByAgencyCode(String agencyCode) {
        return this.routeQueryRepository.findByAgencyCode(agencyCode);
    }

    public RouteEntity findRouteByAgencyCodeAndRouteId(String agencyCode, RouteId routeId) {
        return this.routeQueryRepository.findByAgencyCodeAndLineAndName(agencyCode, routeId.getLine(), routeId.getName());
    }

    @Deprecated
    @Transactional
    public RouteEntity findRouteByNameAndLine(String name, String line) {
        AgencyEntity agencyEntity = this.agencyService.getLoggedAgency();
        return this.routeQueryRepository.findByAgencyCodeAndRouteId(agencyEntity, name, line);
    }

    public Map<Long, RouteEntity> findByExistsBrigade(String agencyCode) {
        return this.routeQueryRepository.findByExistsBrigade(agencyCode).stream()
                .collect(Collectors.toMap(RouteEntity::getRouteId, Function.identity()));
    }

}
