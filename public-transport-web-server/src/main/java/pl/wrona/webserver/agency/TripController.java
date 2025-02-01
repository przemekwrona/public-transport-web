package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.TripApi;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.igeolab.iot.pt.server.api.model.Trips;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@AllArgsConstructor
public class TripController implements TripApi {

    private final TripService tripService;

    @Override
    public ResponseEntity<Status> createTrip(Trip trips) {
        return ResponseEntity.status(CREATED).body(tripService.createTrip(trips));
    }

    @Override
    public ResponseEntity<Trips> getTripByVariant(TripId tripId) {
        return ResponseEntity.ok(tripService.getTripByVariant(tripId));
    }

    @Override
    public ResponseEntity<Trips> getTrips(RouteId routeId) {
        return ResponseEntity.ok(tripService.getTrips(routeId));
    }

    @Override
    public ResponseEntity<Trip> measureDistance(Trip trips) {
        return ResponseEntity.ok(tripService.measureDistance(trips));
    }
}
