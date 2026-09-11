package pl.wrona.webserver.bussiness.route.stop.timetable;

import lombok.AllArgsConstructor;
//import org.igeolab.iot.pt.server.api.RouteStopTimetableApi;
//import org.igeolab.iot.pt.server.api.model.RouteStopTimetable;
import org.igeolab.iot.pt.server.api.RouteStopTimetableApi;
import org.igeolab.iot.pt.server.api.model.RouteStopTimetable;
import org.igeolab.iot.pt.server.api.model.TripMode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class RouteStopTimetableController implements RouteStopTimetableApi {

    private final RouteStopTimetableService routeStopTimetableService;

    @Override
    public ResponseEntity<RouteStopTimetable> getRouteStopTimetable(String agency, String routeCode, String stopCode, TripMode tripMode) {
        return ResponseEntity.ok(routeStopTimetableService.getRouteStopTimetable(agency, routeCode, stopCode, tripMode));
    }
}
