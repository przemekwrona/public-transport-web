package pl.wrona.webserver.bussiness.calendar.deletion;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarQuery;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.trip.core.AgencyService;
import pl.wrona.webserver.bussiness.trip.core.calendar.CalendarDatesRepository;
import pl.wrona.webserver.bussiness.trip.core.calendar.CalendarEntity;
import pl.wrona.webserver.bussiness.trip.core.calendar.CalendarRepository;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class CalendarDeletionService {

    private final CalendarRepository calendarRepository;
    private final CalendarDatesRepository calendarDatesRepository;
    private final AgencyService agencyService;

    @Transactional
    @PreAgencyAuthorize
    public Status deleteCalendarByCalendarName(String instance, CalendarQuery calendarQuery) {
        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);
        calendarRepository.findByAgencyAndCalendarName(agencyEntity, calendarQuery.getCalendarName())
                .ifPresent((CalendarEntity calendarEntity) -> {
                    calendarDatesRepository.deleteByAgencyAndCalendar(agencyEntity, calendarEntity);
                    calendarRepository.delete(calendarEntity);
                });

        return new Status().status(Status.StatusEnum.DELETED);
    }
}
