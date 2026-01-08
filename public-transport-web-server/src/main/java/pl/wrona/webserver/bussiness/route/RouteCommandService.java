package pl.wrona.webserver.bussiness.route;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.RouteEntity;

@Service
@AllArgsConstructor
public class RouteCommandService {
    private final RouteCommandRepository routeCommandRepository;

    @Transactional
    public RouteEntity save(RouteEntity routeEntity) {
        return routeCommandRepository.save(routeEntity);
    }

    @Transactional
    public void deleteByRoute(RouteEntity routeEntity) {
        this.routeCommandRepository.delete(routeEntity);
    }

    public RouteEntity updateRoute(RouteEntity routeEntity) {
        return this.routeCommandRepository.save(routeEntity);
    }
}
