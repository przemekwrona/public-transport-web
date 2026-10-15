package pl.wrona.webserver.core.calendar;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarSymbolId;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class CalendarQueryService {

    private final CalendarSymbolQueryRepository calendarSymbolRepository;

    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER,
            PreAgencyAuthorize.Role.BRIGADIER,
            PreAgencyAuthorize.Role.DISPATCHER,
            PreAgencyAuthorize.Role.DRIVER
    })
    public CalendarSymbolEntity getCalendar(String instance, String calendarName, String designation) {
        return calendarSymbolRepository.findByAgencyAndCalendarNameAndDesignation(instance, calendarName, designation).orElse(null);
    }

    @Deprecated
    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER,
            PreAgencyAuthorize.Role.BRIGADIER,
            PreAgencyAuthorize.Role.DISPATCHER,
            PreAgencyAuthorize.Role.DRIVER
    })
    public CalendarSymbolEntity getCalendar(String instance, CalendarSymbolId calendarId) {
        return calendarSymbolRepository.findByAgencyAndBrigadeAndCalendarAndSymbol(instance, "", calendarId.getCalendarItemId().getCode(), calendarId.getSymbol());
    }
}
