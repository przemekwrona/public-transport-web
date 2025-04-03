package pl.wrona.webserver.agency.calendar;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.CalendarApi;
import org.igeolab.iot.pt.server.api.model.CalendarPayload;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class CalendarController implements CalendarApi {

    private final CalendarService calendarService;

    @Override
    public ResponseEntity<Status> createCalendar(CalendarPayload calendarPayload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarService.createCalendar(calendarPayload));
    }
}
