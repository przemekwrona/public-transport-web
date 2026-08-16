package pl.wrona.webserver.bussiness.route;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.RouteSequenceCommandRepository;
import pl.wrona.webserver.core.agency.RouteSequenceEntity;
import pl.wrona.webserver.core.agency.RouteSequenceQueryRepository;

@Service
@AllArgsConstructor
public class RouteSequenceCommandService {

    private final RouteSequenceCommandRepository routeSequenceCommandRepository;
    private final RouteSequenceQueryRepository routeSequenceQueryRepository;

    @Transactional
    public RouteSequenceEntity init(String agencyCode) {
        var existing = routeSequenceQueryRepository.findByAgencyCode(agencyCode);
        if (existing != null) {
            return existing;
        }
        return routeSequenceCommandRepository.save(new RouteSequenceEntity(agencyCode, 1L));
    }

}
