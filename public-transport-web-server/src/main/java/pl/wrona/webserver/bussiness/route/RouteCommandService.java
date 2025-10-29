package pl.wrona.webserver.bussiness.route;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.RouteEntity;

@Service
@AllArgsConstructor
public class RouteCommandService {
    private final RouteCommandRepository routeCommandRepository;

    public void deleteByRoute(RouteEntity routeEntity) {
        this.routeCommandRepository.delete(routeEntity);
    }
}
