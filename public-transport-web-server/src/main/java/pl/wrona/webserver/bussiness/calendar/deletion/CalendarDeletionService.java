package pl.wrona.webserver.bussiness.calendar.deletion;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.calendar.CalendarDatesRepository;
import pl.wrona.webserver.core.calendar.CalendarSymbolQueryRepository;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class CalendarDeletionService {

    private final CalendarSymbolQueryRepository calendarSymbolRepository;
    private final CalendarDatesRepository calendarDatesRepository;
    private final AgencyService agencyService;
    private final BrigadeGroupQueryService brigadeGroupQueryService;

    @Transactional
    @PreAgencyAuthorize
    public Status deleteCalendarByCalendarName(String instance, String calendarCode, String calendarSymbol) {
        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);
        var calendarSymbolEntity = calendarSymbolRepository.findByAgencyAndBrigadeAndCalendarAndSymbol(instance, calendarCode, calendarSymbol);

        if (calendarSymbolEntity != null) {
            if (brigadeGroupQueryService.existsByCalendarSymbol(calendarSymbolEntity)) {
                throw new BusinessException("ERROR:202609041950", "Can not delete calendar symbol, first delete brigade groups");
            }

            calendarDatesRepository.deleteByAgencyAndCalendar(agencyEntity, calendarSymbolEntity);
            calendarSymbolRepository.delete(calendarSymbolEntity);
        }

        return new Status().status(Status.StatusEnum.DELETED);
    }
}
