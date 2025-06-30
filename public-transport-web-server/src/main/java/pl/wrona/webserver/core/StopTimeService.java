package pl.wrona.webserver.core;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.TripEntity;

import java.util.List;

@Service
@AllArgsConstructor
public class StopTimeService {

    private final StopTimeRepository stopTimeRepository;

    public List<StopTimeEntity> getAllStopTimesByTripId(Long tripId) {
        return stopTimeRepository.findAllByTripId(tripId);
    }

    public List<StopTimeEntity> getAllStopTimesByTrip(TripEntity trip) {
        return stopTimeRepository.findAllByTrip(trip);
    }
}
