package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.agency.entity.StopTimeEntity;

import java.util.List;

@Service
@AllArgsConstructor
public class StopTimeService {

    private final StopTimeRepository stopTimeRepository;

    public List<StopTimeEntity> getAllStopTimesByTripId(Long tripId) {
        return stopTimeRepository.findAllByTripId(tripId);
    }
}
