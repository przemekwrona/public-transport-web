package pl.wrona.webserver.bussiness.calendar;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.CalendarApi;
import org.igeolab.iot.pt.server.api.model.CalendarPayload;
import org.igeolab.iot.pt.server.api.model.CalendarSymbolBody;
import org.igeolab.iot.pt.server.api.model.CreateCalendarItemRequest;
import org.igeolab.iot.pt.server.api.model.CreateCalendarItemResponse;
import org.igeolab.iot.pt.server.api.model.DeleteCalendarItemRequest;
import org.igeolab.iot.pt.server.api.model.GetCalendarItemResponse;
import org.igeolab.iot.pt.server.api.model.GetCalendarSymbolsResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.UpdateCalendarRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.calendar.creator.CalendarCreatorService;
import pl.wrona.webserver.bussiness.calendar.creator.CalendarItemCreatorService;
import pl.wrona.webserver.bussiness.calendar.deletion.CalendarDeletionService;
import pl.wrona.webserver.bussiness.calendar.deletion.CalendarItemDeletionService;
import pl.wrona.webserver.bussiness.calendar.item.reader.CalendarSymbolReaderService;
import pl.wrona.webserver.bussiness.calendar.item.reader.CalendarItemReaderService;
import pl.wrona.webserver.bussiness.calendar.item.reader.CalendarReaderService;
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
    private final CalendarItemDeletionService calendarItemDeletionService;
    private final CalendarSymbolReaderService calendarSymbolReaderService;

    @Override
    public ResponseEntity<Status> createCalendar(String agency, CalendarPayload calendarPayload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarCreatorService.createCalendar(agency, calendarPayload));
    }

    @Override
    public ResponseEntity<CreateCalendarItemResponse> createCalendarItem(String agency, CreateCalendarItemRequest createCalendarItemRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarItemCreatorService.createCalendarItem(agency, createCalendarItemRequest));
    }

    @Override
    public ResponseEntity<Status> deleteCalendarByCalendarNameAndSymbol(String agency, String calendarCode, String calendarSymbol) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(calendarDeletionService.deleteCalendarByCalendarName(agency, calendarCode, calendarSymbol));    }

    @Override
    public ResponseEntity<Status> deleteCalendarItem(String agency, DeleteCalendarItemRequest deleteCalendarItemRequest) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(calendarItemDeletionService.deleteCalendarItem(agency, deleteCalendarItemRequest));
    }

    @Override
    public ResponseEntity<GetCalendarItemResponse> getCalendarByCalendarCode(String agency, String calendarCode) {
        return ResponseEntity.ok(calendarItemReaderService.getCalendarByCalendarCode(agency, calendarCode));
    }

    @Override
    public ResponseEntity<GetCalendarItemResponse> getCalendarItems(String agency) {
        return ResponseEntity.ok(calendarItemReaderService.getCalendarItems(agency));
    }

    @Override
    public ResponseEntity<CalendarSymbolBody> getCalendarSymbol(String agency, String calendarCode, String calendarSymbol) {
        return ResponseEntity.ok(calendarSymbolReaderService.getCalendarSymbol(agency, calendarCode, calendarSymbol));
    }

    @Override
    public ResponseEntity<GetCalendarSymbolsResponse> getCalendars(String agency) {
        return ResponseEntity.ok(calendarReaderService.getCalendars(agency));
    }

    @Override
    public ResponseEntity<Status> updateCalendar(String agency, UpdateCalendarRequest updateCalendarRequest) {
        return ResponseEntity.ok(calendarUpdaterService.updateCalendar(agency, updateCalendarRequest));
    }

}
