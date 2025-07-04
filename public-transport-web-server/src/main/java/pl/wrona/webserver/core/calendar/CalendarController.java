package pl.wrona.webserver.core.calendar;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.CalendarApi;
import org.igeolab.iot.pt.server.api.model.CalendarBody;
import org.igeolab.iot.pt.server.api.model.CalendarPayload;
import org.igeolab.iot.pt.server.api.model.CalendarQuery;
import org.igeolab.iot.pt.server.api.model.GetCalendarsResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.UpdateCalendarRequest;
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

    @Override
    public ResponseEntity<Status> deleteCalendarByCalendarName(CalendarQuery calendarQuery) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(calendarService.deleteCalendarByCalendarName(calendarQuery));
    }

    @Override
    public ResponseEntity<CalendarBody> getCalendarByCalendarName(CalendarQuery calendarQuery) {
        return ResponseEntity.status(HttpStatus.OK).body(calendarService.getCalendarByCalendarName(calendarQuery));
    }

    @Override
    public ResponseEntity<GetCalendarsResponse> getCalendars() {
        return ResponseEntity.ok(calendarService.getCalendars());
    }

    @Override
    public ResponseEntity<Status> updateCalendar(UpdateCalendarRequest updateCalendarRequest) {
        return ResponseEntity.ok(calendarService.updateCalendar(updateCalendarRequest));
    }


}
