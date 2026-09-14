package pl.wrona.webserver.bussiness.route.stop.timetable;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.RouteStopTimetableApi;
import org.igeolab.iot.pt.server.api.model.TripMode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class RouteStopTimetableController implements RouteStopTimetableApi {

    private final RouteStopTimetableService routeStopTimetableService;
    private final HttpServletRequest httpServletRequest;

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResponseEntity getRouteStopTimetable(String agency, String routeCode, String stopCode, TripMode tripMode) {
        if (RouteStopTimetableService.wantsPdf(httpServletRequest.getHeader(HttpHeaders.ACCEPT))) {
            return routeStopTimetableService.getRouteStopTimetablePdf(agency, routeCode, stopCode, tripMode);
        }
        return ResponseEntity.ok(routeStopTimetableService.getRouteStopTimetable(agency, routeCode, stopCode, tripMode));
    }
}
