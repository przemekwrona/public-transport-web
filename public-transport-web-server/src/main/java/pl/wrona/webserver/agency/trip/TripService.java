package pl.wrona.webserver.agency.trip;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.agency.entity.Trip;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class TripService {

    private final TripRepository tripRepository;

    public List<Trip> save(Set<Trip> trips) {
        return tripRepository.saveAll(trips);
    }
}
