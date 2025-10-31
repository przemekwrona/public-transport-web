package pl.wrona.webserver.bussiness.trip;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.TripDistanceMeasuresApi;
import org.igeolab.iot.pt.server.api.model.TripMeasure;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.trip.measure.TripDistanceMeasureService;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class TripDistanceMeasureController implements TripDistanceMeasuresApi {

    private final TripDistanceMeasureService tripDistanceMeasureService;

    @Override
    public ResponseEntity<TripMeasure> approximateDistance(TripMeasure tripMeasure) {
        return ResponseEntity.ok(tripDistanceMeasureService.approximateDistance(tripMeasure));
    }

    @Override
    public ResponseEntity<TripMeasure> measureDistance(TripMeasure tripMeasure) {
        return ResponseEntity.ok(tripDistanceMeasureService.measureDistance(tripMeasure));
    }
}
