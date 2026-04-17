package pl.wrona.webserver.core.calendar;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class CalendarQueryService {

    private final CalendarRepository calendarRepository;

    @PreAgencyAuthorize
    public CalendarSymbolEntity getCalendar(String instance, String calendarName) {
        return calendarRepository.findByAgencyAndCalendarName(instance, calendarName).orElse(null);
    }
}
