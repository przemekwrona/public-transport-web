package pl.wrona.webserver.bussiness.trip;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;

@Service
@AllArgsConstructor
public class TripService {

    private final TripRepository tripRepository;

    @Deprecated
    public TripEntity findByTripId(TripId tripId) {
        return tripRepository.findByLineAndNameAndVariantAndMode(tripId.getRouteId().getLine(), tripId.getRouteId().getName(), tripId.getVariantName(), TripVariantModeMapper.map(tripId.getVariantMode()));
    }
}
