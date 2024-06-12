package pl.wrona.webserver.gtfs;

import lombok.AllArgsConstructor;
import org.igeolab.iot.gtfs.server.api.ShapeApi;
import org.igeolab.iot.gtfs.server.api.StopsApi;
import org.igeolab.iot.gtfs.server.api.TimetableApi;
import org.igeolab.iot.gtfs.server.api.model.ShapeResponse;
import org.igeolab.iot.gtfs.server.api.model.Stops;
import org.igeolab.iot.gtfs.server.api.model.Timetables;
import org.igeolab.iot.otp.api.model.RoutesResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.otp.OtpClient;
import pl.wrona.webserver.otp.OtpService;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class GtfsController implements StopsApi, TimetableApi, ShapeApi {

    private final GtfsService gtfsService;

    @Override
    public ResponseEntity<Stops> getStops(String agencyCode, Double latitude, Double longitude) {
        return gtfsService.getStops(agencyCode, latitude, longitude);
    }

    @Override
    public ResponseEntity<Timetables> getTimetableByStopId(String agencyCode, String stopId) {
        return gtfsService.getTimetableByStopId(agencyCode, stopId);
    }

    @Override
    public ResponseEntity<ShapeResponse> getShapesByStopId(String agencyCode, String stopId) {
        return gtfsService.getShapesByStopId(agencyCode, stopId);
    }

}
