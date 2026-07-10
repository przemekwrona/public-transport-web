package pl.wrona.webserver.core.calendar;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarId;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class CalendarQueryService {

    private final CalendarSymbolQueryRepository calendarSymbolRepository;

    @PreAgencyAuthorize
    public CalendarSymbolEntity getCalendar(String instance, String calendarName, String designation) {
        return calendarSymbolRepository.findByAgencyAndCalendarNameAndDesignation(instance, calendarName, designation).orElse(null);
    }

    @PreAgencyAuthorize
    public CalendarSymbolEntity getCalendar(String instance, CalendarId calendarId) {
        return calendarSymbolRepository.findByAgencyAndCalendarNameAndDesignation(instance, calendarId.getName(), calendarId.getSymbol()).orElse(null);
    }
}
