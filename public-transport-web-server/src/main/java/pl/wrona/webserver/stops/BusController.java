package pl.wrona.webserver.stops;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.StopsApi;
import org.igeolab.iot.pt.server.api.model.CenterPoint;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.StopsPatchRequest;
import org.igeolab.iot.pt.server.api.model.StopsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class BusController implements StopsApi {

    private final BusStopService busStopService;

    @Override
    public ResponseEntity<CenterPoint> centerMap() {
        return ResponseEntity.ok(busStopService.centerMap());
    }

    @Override
    public ResponseEntity<StopsResponse> findStopsByStopName(String stopName) {
        return ResponseEntity.ok(busStopService.findStopsByStopName(stopName));
    }

    @Override
    public ResponseEntity<StopsResponse> getStopsInArea(Float maxLat, Float minLon, Float minLat, Float maxLon) {
        return ResponseEntity.ok(busStopService.findBusStop(maxLat, minLon, minLat, maxLon));
    }

    @Override
    public ResponseEntity<Status> patchStop(StopsPatchRequest stopsPatchRequest) {
        return ResponseEntity.ok(busStopService.patchStop(stopsPatchRequest));
    }
}
