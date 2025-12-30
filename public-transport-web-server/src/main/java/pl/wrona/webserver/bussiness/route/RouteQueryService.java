package pl.wrona.webserver.bussiness.route;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RouteQueryService {

    private final RouteQueryRepository routeQueryRepository;

    @PreAgencyAuthorize
    public List<RouteEntity> findByAgencyCode(String instance) {
        return this.routeQueryRepository.findByAgencyCode(instance);
    }

    @PreAgencyAuthorize
    public RouteEntity findRouteByAgencyCodeAndRouteId(String instance, RouteId routeId) {
        return this.routeQueryRepository.findByAgencyCodeAndLineAndName(instance, routeId.getLine(), routeId.getName());
    }

    public Map<Long, RouteEntity> findByExistsBrigade(String agencyCode) {
        return this.routeQueryRepository.findByExistsBrigade(agencyCode).stream()
                .collect(Collectors.toMap(RouteEntity::getRouteId, Function.identity()));
    }

}
