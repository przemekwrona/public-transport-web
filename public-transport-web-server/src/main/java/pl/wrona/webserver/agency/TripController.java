package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.TripApi;
import org.igeolab.iot.pt.server.api.model.GetAllTripsResponse;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.igeolab.iot.pt.server.api.model.Trips;
import org.igeolab.iot.pt.server.api.model.TripsDetails;
import org.igeolab.iot.pt.server.api.model.UpdateTripDetailsRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@AllArgsConstructor
public class TripController implements TripApi {

    private final TripService tripService;
    private final TripQueryService tripQueryService;

    @Override
    public ResponseEntity<Status> createTrip(Trip trips) {
        return ResponseEntity.status(CREATED).body(tripService.createTrip(trips));
    }

    @Override
    public ResponseEntity<Status> deleteTripByTripId(TripId tripId) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(tripService.deleteTripByTripId(tripId));
    }

    @Override
    public ResponseEntity<Status> updateTrip(UpdateTripDetailsRequest updateTripDetailsRequest) {
        return ResponseEntity.status(CREATED).body(tripService.updateTrip(updateTripDetailsRequest));
    }

    @Override
    public ResponseEntity<TripsDetails> getTripByVariant(TripId tripId) {
        return ResponseEntity.ok(tripService.getTripByVariant(tripId));
    }

    @Override
    public ResponseEntity<Trips> getTrips(RouteId routeId) {
        return ResponseEntity.ok(tripService.getTrips(routeId));
    }

    @Override
    public ResponseEntity<GetAllTripsResponse> getTripsByLineOrName(String filter) {
        return ResponseEntity.ok(tripQueryService.getTripsByLineOrName(filter));
    }

    @Override
    public ResponseEntity<Trip> measureDistance(Trip trips) {
        return ResponseEntity.ok(tripService.measureDistance(trips));
    }
}
