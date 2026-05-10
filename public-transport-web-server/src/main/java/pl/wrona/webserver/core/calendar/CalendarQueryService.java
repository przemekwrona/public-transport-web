package pl.wrona.webserver.core.calendar;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class CalendarQueryService {

    private final CalendarSymbolQueryRepository calendarSymbolRepository;

    @PreAgencyAuthorize
    public CalendarSymbolEntity getCalendar(String instance, String calendarName) {
        return calendarSymbolRepository.findByAgencyAndCalendarName(instance, calendarName).orElse(null);
    }
}
