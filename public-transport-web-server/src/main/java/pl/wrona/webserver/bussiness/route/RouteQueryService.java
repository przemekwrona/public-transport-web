package pl.wrona.webserver.bussiness.route;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        return this.routeQueryRepository.findByAgencyCodeAndLineAndName(instance, routeId.getLine(), routeId.getName(), routeId.getVersion());
    }

    public Map<Long, RouteEntity> findByExistsBrigade(String agencyCode) {
        return this.routeQueryRepository.findByExistsBrigade(agencyCode).stream()
                .collect(Collectors.toMap(RouteEntity::getRouteId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public int findLastInsertedVersion(String instance, String line, String name) {
        Integer lastInsertedVersion = this.routeQueryRepository.findLastInsertedVersion(instance, line, name);
        return lastInsertedVersion == null ? 0 : lastInsertedVersion;
    }


}
