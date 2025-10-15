package pl.wrona.webserver.bussiness.calendar;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.calendar.creator.CalendarCreatorService;
import pl.wrona.webserver.bussiness.calendar.deletion.CalendarDeletionService;
import pl.wrona.webserver.bussiness.calendar.reader.CalendarReaderService;
import pl.wrona.webserver.bussiness.calendar.updater.CalendarUpdaterService;
import pl.wrona.webserver.core.calendar.CalendarService;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class CalendarController implements CalendarApi {

    private final CalendarService calendarService;
    private final CalendarCreatorService calendarCreatorService;
    private final CalendarReaderService calendarReaderService;
    private final CalendarDeletionService calendarDeletionService;
    private final CalendarUpdaterService calendarUpdaterService;

    @Override
    public ResponseEntity<Status> createCalendar(String agency, CalendarPayload calendarPayload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarCreatorService.createCalendar(agency, calendarPayload));
    }

    @Override
    public ResponseEntity<Status> deleteCalendarByCalendarName(String agency, CalendarQuery calendarQuery) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(calendarDeletionService.deleteCalendarByCalendarName(agency, calendarQuery));
    }

    @Override
    public ResponseEntity<CalendarBody> getCalendarByCalendarName(CalendarQuery calendarQuery) {
        return ResponseEntity.status(HttpStatus.OK).body(calendarService.getCalendarByCalendarName(calendarQuery));
    }

    @Override
    public ResponseEntity<GetCalendarsResponse> getCalendars(String agency) {
        return ResponseEntity.ok(calendarReaderService.getCalendars(agency));
    }

    @Override
    public ResponseEntity<Status> updateCalendar(String agency, UpdateCalendarRequest updateCalendarRequest) {
        return ResponseEntity.ok(calendarUpdaterService.updateCalendar(agency, updateCalendarRequest));
    }


}
