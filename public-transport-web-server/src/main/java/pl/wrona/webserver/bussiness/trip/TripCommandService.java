package pl.wrona.webserver.bussiness.trip;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.trip.core.agency.RouteEntity;

@Service
@AllArgsConstructor
public class TripCommandService {

    private final TripCommandRepository tripCommandRepository;

    public void deleteTripByRoute(RouteEntity route) {
        this.tripCommandRepository.deleteAllByRoute(route);
    }


}
