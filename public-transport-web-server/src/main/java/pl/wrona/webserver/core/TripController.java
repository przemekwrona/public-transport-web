package pl.wrona.webserver.core;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.TripApi;
import org.igeolab.iot.pt.server.api.model.CreateTripDetailsRequest;
import org.igeolab.iot.pt.server.api.model.GetAllTripsResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.igeolab.iot.pt.server.api.model.TripsDetails;
import org.igeolab.iot.pt.server.api.model.UpdateTripDetailsRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.trip.creator.TripCreatorService;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class TripController implements TripApi {

    private final TripService tripService;
    private final TripRefactorQueryService tripRefactorQueryService;
    private final TripCreatorService tripCreatorService;

    @Override
    public ResponseEntity<Status> createTrip(String agency, CreateTripDetailsRequest createTripDetailsRequest) {
        return ResponseEntity.status(CREATED).body(tripCreatorService.createTrip(agency, createTripDetailsRequest));
    }

    @Override
    public ResponseEntity<Status> deleteTripByTripId(String agency, TripId tripId) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(tripService.deleteTripByTripId(tripId));
    }

    @Override
    public ResponseEntity<Status> updateTrip(String agency, UpdateTripDetailsRequest updateTripDetailsRequest) {
        return ResponseEntity.status(CREATED).body(tripService.updateTrip(updateTripDetailsRequest));
    }

    @Override
    public ResponseEntity<TripsDetails> getTripByVariant(TripId tripId) {
        return ResponseEntity.ok(tripService.getTripByVariant(tripId));
    }

    @Override
    public ResponseEntity<GetAllTripsResponse> getTripsByLineOrName(String agency, String filter) {
        return ResponseEntity.ok(tripRefactorQueryService.getTripsByLineOrName(filter));
    }
}
