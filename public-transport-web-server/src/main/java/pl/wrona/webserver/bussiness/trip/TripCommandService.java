package pl.wrona.webserver.bussiness.trip;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.TripEntity;

@Service
@AllArgsConstructor
public class TripCommandService {

    private final TripCommandRepository tripCommandRepository;

    public void deleteTripByRoute(RouteEntity route) {
        this.tripCommandRepository.deleteAllByRoute(route);
    }

    public void deleteTrip(TripEntity tripId) {
        tripCommandRepository.deleteById(tripId.getTripId());
    }

    public TripEntity save(TripEntity entity) {
        return tripCommandRepository.save(entity);
    }


}
