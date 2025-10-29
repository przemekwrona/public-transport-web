package pl.wrona.webserver.bussiness.trip.core;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.trip.core.agency.StopTimeEntity;
import pl.wrona.webserver.bussiness.trip.core.agency.TripEntity;

import java.util.List;

@Service
@AllArgsConstructor
public class StopTimeService {

    private final StopTimeRepository stopTimeRepository;

    public List<StopTimeEntity> getAllStopTimesByTrip(TripEntity trip) {
        return stopTimeRepository.findAllByTrip(trip);
    }
}
