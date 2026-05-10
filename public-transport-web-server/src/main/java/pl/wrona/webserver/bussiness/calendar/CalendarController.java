package pl.wrona.webserver.bussiness.calendar;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.CalendarApi;
import org.igeolab.iot.pt.server.api.model.CalendarBody;
import org.igeolab.iot.pt.server.api.model.CalendarPayload;
import org.igeolab.iot.pt.server.api.model.CalendarSymbolQuery;
import org.igeolab.iot.pt.server.api.model.CreateCalendarItemRequest;
import org.igeolab.iot.pt.server.api.model.CreateCalendarItemResponse;
import org.igeolab.iot.pt.server.api.model.GetCalendarItemResponse;
import org.igeolab.iot.pt.server.api.model.GetCalendarsResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.UpdateCalendarRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.calendar.creator.CalendarCreatorService;
import pl.wrona.webserver.bussiness.calendar.creator.CalendarItemCreatorService;
import pl.wrona.webserver.bussiness.calendar.deletion.CalendarDeletionService;
import pl.wrona.webserver.bussiness.calendar.reader.CalendarItemReaderService;
import pl.wrona.webserver.bussiness.calendar.reader.CalendarReaderService;
import pl.wrona.webserver.bussiness.calendar.updater.CalendarUpdaterService;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class CalendarController implements CalendarApi {

    private final CalendarCreatorService calendarCreatorService;
    private final CalendarReaderService calendarReaderService;
    private final CalendarUpdaterService calendarUpdaterService;
    private final CalendarDeletionService calendarDeletionService;
    private final CalendarItemCreatorService calendarItemCreatorService;
    private final CalendarItemReaderService calendarItemReaderService;

    @Override
    public ResponseEntity<Status> createCalendar(String agency, CalendarPayload calendarPayload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarCreatorService.createCalendar(agency, calendarPayload));
    }

    @Override
    public ResponseEntity<CreateCalendarItemResponse> createCalendarItem(String agency, CreateCalendarItemRequest createCalendarItemRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarItemCreatorService.createCalendarItem(agency, createCalendarItemRequest));
    }

    @Override
    public ResponseEntity<Status> deleteCalendarByCalendarNameAndSymbol(String agency, CalendarSymbolQuery calendarSymbolQuery) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(calendarDeletionService.deleteCalendarByCalendarName(agency, calendarSymbolQuery));
    }

    @Override
    public ResponseEntity<CalendarBody> getCalendarByCalendarName(String agency, CalendarSymbolQuery calendarSymbolQuery) {
        return ResponseEntity.status(HttpStatus.OK).body(calendarReaderService.getCalendarByCalendarName(agency, calendarSymbolQuery));
    }

    @Override
    public ResponseEntity<GetCalendarItemResponse> getCalendarItems(String agency) {
        return ResponseEntity.ok(calendarItemReaderService.getCalendarItems(agency));
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
