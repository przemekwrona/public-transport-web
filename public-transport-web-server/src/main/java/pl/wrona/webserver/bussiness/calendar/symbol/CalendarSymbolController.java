package pl.wrona.webserver.bussiness.calendar.symbol;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.CalendarSymbolApi;
import org.igeolab.iot.pt.server.api.model.GetCalendarSymbolsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.calendar.symbol.pagination.CalendarSymbolPaginationService;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class CalendarSymbolController implements CalendarSymbolApi {

    private final CalendarSymbolPaginationService paginationService;

    @Override
    public ResponseEntity<GetCalendarSymbolsResponse> getCalendarSymbolsByCalendarCode(String agency, String calendarCode) {
        return ResponseEntity.ok(paginationService.getCalendarSymbolsByCalendarCode(agency, calendarCode));
    }
}
