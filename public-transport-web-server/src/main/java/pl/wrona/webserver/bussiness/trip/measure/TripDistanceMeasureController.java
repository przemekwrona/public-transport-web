package pl.wrona.webserver.bussiness.trip.measure;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.TripDistanceMeasuresApi;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TripDistanceMeasureController implements TripDistanceMeasuresApi {

    private final TripDistanceMeasureService tripDistanceMeasureService;

    @Override
    public ResponseEntity<Trip> approximateDistance(Trip trip) {
        return ResponseEntity.ok(tripDistanceMeasureService.approximateDistance(trip));
    }

    @Override
    public ResponseEntity<Trip> measureDistance(Trip trip) {
        return ResponseEntity.ok(tripDistanceMeasureService.measureDistance(trip));
    }
}
