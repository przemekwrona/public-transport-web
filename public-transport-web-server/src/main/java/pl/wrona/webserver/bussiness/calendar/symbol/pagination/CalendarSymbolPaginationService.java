package pl.wrona.webserver.bussiness.calendar.symbol.pagination;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.GetCalendarSymbolsResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.calendar.CalendarDatesQueryService;
import pl.wrona.webserver.bussiness.calendar.mapper.CalendarBodyMapper;
import pl.wrona.webserver.bussiness.calendar.symbol.CalendarSymbolQueryService;

import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CalendarSymbolPaginationService {

    private final CalendarSymbolQueryService calendarSymbolQueryService;
    private final CalendarDatesQueryService calendarDatesQueryService;

    public GetCalendarSymbolsResponse getCalendarSymbolsByCalendarCode(String agency, String calendarCode) {
        var calendarSymbols = calendarSymbolQueryService.findByAgencyAndCalendarCode(agency, calendarCode);

        var dateDictionary = calendarDatesQueryService.findAllByCalendar(calendarSymbols).stream()
                .collect(Collectors.groupingBy(entity -> entity.getCalendar().getServiceId()));

        var calendars = calendarSymbols.stream()
                .map(symbol -> CalendarBodyMapper.apply(symbol.getCalendarItem(), symbol, dateDictionary))
                .toList();

        return new GetCalendarSymbolsResponse()
                .calendars(calendars);
    }
}
