package pl.wrona.webserver.bussiness.calendar.reader;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarBody;
import org.igeolab.iot.pt.server.api.model.CalendarQuery;
import org.igeolab.iot.pt.server.api.model.GetCalendarsResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.calendar.CalendarItemQueryService;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.bussiness.calendar.mapper.CalendarBodyMapper;
import pl.wrona.webserver.core.calendar.CalendarDatesEntity;
import pl.wrona.webserver.core.calendar.CalendarDatesQueryService;
import pl.wrona.webserver.core.calendar.CalendarDatesRepository;
import pl.wrona.webserver.core.calendar.CalendarSymbolQueryService;
import pl.wrona.webserver.core.calendar.CalendarSymbolRepository;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CalendarReaderService {

    private final CalendarSymbolRepository calendarSymbolRepository;
    private final CalendarDatesRepository calendarDatesRepository;
    private final AgencyService agencyService;
    private final CalendarItemQueryService calendarItemQueryService;
    private final CalendarSymbolQueryService calendarSymbolQueryService;
    private final CalendarDatesQueryService calendarDatesQueryService;

    @PreAgencyAuthorize
    public GetCalendarsResponse getCalendars(String instance) {
        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);
        Map<Long, List<CalendarDatesEntity>> calendarDatesDictionary = calendarDatesRepository.findAllByAgency(agencyEntity).stream()
                .collect(Collectors.groupingBy(calendarDates -> calendarDates.getCalendarDatesId().getServiceId()));

        var calendars = calendarSymbolRepository.findAllByAgency(agencyEntity).stream()
                .map(calendar -> CalendarBodyMapper.apply(calendar.getCalendarItem(), calendar, calendarDatesDictionary))
                .toList();

        return new GetCalendarsResponse()
                .calendars(calendars);
    }

    @PreAgencyAuthorize
    public CalendarBody getCalendarByCalendarName(String instance, CalendarQuery calendarQuery) {
        var item = calendarItemQueryService.findByAgencyAndStartDateAndEndDate(instance, calendarQuery.getStartDate(), calendarQuery.getEndDate());
        var symbol = calendarSymbolQueryService.findCalendarByCalendarCode(item, calendarQuery.getDesignation());
        var dates = calendarDatesQueryService.findAllByCalendar(symbol);

        Map<Long, List<CalendarDatesEntity>> calendarDatesDictionary = Map.of(symbol.getServiceId(), dates);
        return CalendarBodyMapper.apply(item, symbol, calendarDatesDictionary);
    }

}
