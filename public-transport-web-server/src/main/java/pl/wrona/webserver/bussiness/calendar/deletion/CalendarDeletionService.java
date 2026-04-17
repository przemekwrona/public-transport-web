package pl.wrona.webserver.bussiness.calendar.deletion;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarQuery;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.calendar.CalendarDatesRepository;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;
import pl.wrona.webserver.core.calendar.CalendarSymbolRepository;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class CalendarDeletionService {

    private final CalendarSymbolRepository calendarSymbolRepository;
    private final CalendarDatesRepository calendarDatesRepository;
    private final AgencyService agencyService;

    @Transactional
    @PreAgencyAuthorize
    public Status deleteCalendarByCalendarName(String instance, CalendarQuery calendarQuery) {
        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);
        calendarSymbolRepository.findByAgencyAndCalendarName(instance, calendarQuery.getCalendarName())
                .ifPresent((CalendarSymbolEntity calendarSymbolEntity) -> {
                    calendarDatesRepository.deleteByAgencyAndCalendar(agencyEntity, calendarSymbolEntity);
                    calendarSymbolRepository.delete(calendarSymbolEntity);
                });

        return new Status().status(Status.StatusEnum.DELETED);
    }
}
