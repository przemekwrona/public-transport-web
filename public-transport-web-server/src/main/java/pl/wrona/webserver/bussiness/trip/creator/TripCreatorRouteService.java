package pl.wrona.webserver.bussiness.trip.creator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.trip.core.agency.AgencyEntity;
import pl.wrona.webserver.bussiness.trip.core.agency.RouteEntity;

@Service
@AllArgsConstructor
public class TripCreatorRouteService {

    private final TripCreatorRouteRepository tripCreatorRouteRepository;

    RouteEntity findRouteByAgencyAndNameAndLine(AgencyEntity agency, String name, String line) {
        return tripCreatorRouteRepository.findByAgencyAndNameAndLine(agency, name, line);
    }
}
