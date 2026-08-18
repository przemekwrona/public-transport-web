package pl.wrona.webserver.bussiness.trip;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.agency.TripTrafficMode;

import java.util.List;

@Service
@AllArgsConstructor
public class TripProfileQueryService {

    private final TripProfileQueryRepository tripProfileQueryRepository;

    public List<TripProfileEntity> findAllByTrip(TripEntity trip) {
        return tripProfileQueryRepository.findAllByTrip(trip);
    }

    public TripProfileEntity findAllByTripAndTrafficMode(TripEntity trip, TripTrafficMode trafficMode) {
        return tripProfileQueryRepository.findAllByTripAndTrafficMode(trip, trafficMode);
    }

}
