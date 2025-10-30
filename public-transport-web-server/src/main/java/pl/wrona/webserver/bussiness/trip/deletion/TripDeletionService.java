package pl.wrona.webserver.bussiness.trip.deletion;


import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.trip.TripCommandService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.bussiness.trip.TripRepository;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.mapper.TripModeMapper;

@Service
@AllArgsConstructor
public class TripDeletionService {

    private final TripCommandService tripCommandService;
    private final TripQueryService tripQueryService;

    @Transactional
    public Status deleteTripByTripId(TripId tripId) {
//        if (tripQueryService.existsTripInBrigade() == null) {}
//        TripEntity deleteTrip = tripRepository.findByLineAndNameAndVariantAndMode(tripId.getLine(), tripId.getName(), tripId.getVariant(), TripModeMapper.map(tripId.getMode()));
//        tripRepository.delete(deleteTrip);
        return new Status().status(Status.StatusEnum.DELETED);
    }
}
