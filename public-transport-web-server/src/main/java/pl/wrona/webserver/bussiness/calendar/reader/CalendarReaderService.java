package pl.wrona.webserver.bussiness.calendar.reader;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarBody;
import org.igeolab.iot.pt.server.api.model.CalendarQuery;
import org.igeolab.iot.pt.server.api.model.GetCalendarsResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.bussiness.calendar.mapper.CalendarBodyMapper;
import pl.wrona.webserver.core.calendar.CalendarDatesEntity;
import pl.wrona.webserver.core.calendar.CalendarDatesRepository;
import pl.wrona.webserver.core.calendar.CalendarRepository;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CalendarReaderService {

    private final CalendarRepository calendarRepository;
    private final CalendarDatesRepository calendarDatesRepository;
    private final AgencyService agencyService;

    @PreAgencyAuthorize
    public GetCalendarsResponse getCalendars(String instance) {
        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);
        Map<Long, List<CalendarDatesEntity>> calendarDatesDictionary = calendarDatesRepository.findAllByAgency(agencyEntity).stream()
                .collect(Collectors.groupingBy(calendarDates -> calendarDates.getCalendarDatesId().getServiceId()));

        var calendars = calendarRepository.findAllByAgency(agencyEntity).stream()
                .map(calendar -> CalendarBodyMapper.apply(calendar, calendarDatesDictionary))
                .toList();

        return new GetCalendarsResponse()
                .calendars(calendars);
    }

    @PreAgencyAuthorize
    public CalendarBody getCalendarByCalendarName(String instance, CalendarQuery calendarQuery) {
        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);
        Map<Long, List<CalendarDatesEntity>> calendarDatesDictionary = calendarDatesRepository.findAllByAgency(agencyEntity).stream()
                .collect(Collectors.groupingBy(calendarDates -> calendarDates.getCalendarDatesId().getServiceId()));

        return calendarRepository.findByAgencyAndCalendarName(instance, calendarQuery.getCalendarName())
                .map(calendar -> CalendarBodyMapper.apply(calendar, calendarDatesDictionary))
                .orElseThrow();
    }

}
