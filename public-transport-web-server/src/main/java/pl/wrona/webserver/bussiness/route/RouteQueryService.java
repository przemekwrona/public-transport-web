package pl.wrona.webserver.bussiness.route;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.RouteEntity;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RouteQueryService {

    private final AgencyService agencyService;
    private final RouteQueryRepository routeQueryRepository;

    public RouteEntity findRouteByAgencyCodeAndLineAndName(String agencyCode, String line, String name) {
        return this.routeQueryRepository.findByAgencyCodeAndLineAndName(agencyCode, line, name);
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

    public Map<Long, RouteEntity> findByExistsBrigade(String agencyCode) {
        return this.routeQueryRepository.findByExistsBrigade(agencyCode).stream()
                .collect(Collectors.toMap(RouteEntity::getRouteId, Function.identity()));
    }

}
