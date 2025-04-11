package pl.wrona.webserver.stops;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.StopsApi;
import org.igeolab.iot.pt.server.api.model.StopsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class BusController implements StopsApi {

    private final BusStopService busStopService;

    @Override
    public ResponseEntity<StopsResponse> findStopsByStopName(String stopName) {
        return ResponseEntity.ok(busStopService.findStopsByStopName(stopName));
    }

    @Override
    public ResponseEntity<StopsResponse> getStopsInArea(Float maxLat, Float minLon, Float minLat, Float maxLon) {
        return ResponseEntity.ok(busStopService.findBusStop(maxLat, minLon, minLat, maxLon));
    }
}
