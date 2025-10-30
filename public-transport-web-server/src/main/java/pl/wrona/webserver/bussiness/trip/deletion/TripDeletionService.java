package pl.wrona.webserver.bussiness.trip.deletion;


import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.trip.TripCommandService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.exception.BusinessException;

@Service
@AllArgsConstructor
public class TripDeletionService {

    private final TripCommandService tripCommandService;
    private final TripQueryService tripQueryService;

    @Transactional
    public Status deleteTripByTripId(String instance, TripId tripId) {
        TripEntity savedTripEntity = tripQueryService.findByAgencyCodeAndTripId(instance, tripId);

        if (savedTripEntity == null) {
            throw new BusinessException("ERROR:202510301510", "Trip with id " + tripId + " not found");
        }

        if (tripQueryService.existsTripInBrigade(savedTripEntity)) {
            throw new BusinessException("ERROR:202510301511", "Trip with id " + tripId + " already exists in Brigade");
        } else {
            tripCommandService.deleteTrip(savedTripEntity);
        }
        return new Status().status(Status.StatusEnum.DELETED);
    }
}
