package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.GetAllTripsResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.agency.entity.TripEntity;

import java.util.List;

@Service
@AllArgsConstructor
public class TripQueryService {

    private TripRepository tripRepository;

    public GetAllTripsResponse getTripsByLineOrName(String lineOrName) {
        List<TripEntity> trips = tripRepository.findByLineOrNameContainingIgnoreCase(lineOrName);

        return new GetAllTripsResponse()
                .lines(List.of());
    }
}
