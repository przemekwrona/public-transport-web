package pl.wrona.webserver.gtfs;

import lombok.AllArgsConstructor;
import org.igeolab.iot.gtfs.server.api.model.ShapeResponse;
import org.igeolab.iot.gtfs.server.api.model.Stops;
import org.igeolab.iot.gtfs.server.api.model.Timetables;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GtfsService {
    private final WarsawGtfsStopsClient warsawGtfsStopsClient;
    private final WarsawGtfsTimetableClient warsawGtfsTimetableClient;
    private final WarsawGtfsShapeClient warsawGtfsShapeClient;

    public ResponseEntity<Stops> getStops(String agencyCode, Double latitude, Double longitude) {
        return switch (agencyCode) {
            case "WAWA" -> warsawGtfsStopsClient.getStops(agencyCode, latitude, longitude);
            default -> warsawGtfsStopsClient.getStops(agencyCode, latitude, longitude);
        };
    }

    public ResponseEntity<Timetables> getTimetableByStopId(String agencyCode, String stopId) {
        return switch (agencyCode) {
            case "WAWA" -> warsawGtfsTimetableClient.getTimetableByStopId(agencyCode, stopId);
            default -> warsawGtfsTimetableClient.getTimetableByStopId(agencyCode, stopId);
        };
    }

    public ResponseEntity<ShapeResponse> getShapesByStopId(String agencyCode, String stopId) {
        return switch (agencyCode) {
            case "WAWA" -> warsawGtfsShapeClient.getShapesByStopId(agencyCode, stopId);
            default -> warsawGtfsShapeClient.getShapesByStopId(agencyCode, stopId);
        };
    }

}
