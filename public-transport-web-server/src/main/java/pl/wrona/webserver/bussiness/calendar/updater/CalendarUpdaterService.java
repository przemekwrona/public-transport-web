package pl.wrona.webserver.bussiness.calendar.updater;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.UpdateCalendarRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.calendar.CalendarDatesEntity;
import pl.wrona.webserver.bussiness.calendar.mapper.CalendarDatesEntityMapper;
import pl.wrona.webserver.core.calendar.CalendarDatesRepository;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;
import pl.wrona.webserver.bussiness.calendar.mapper.CalendarEntityMapper;
import pl.wrona.webserver.core.calendar.CalendarRepository;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.Set;

@Service
@AllArgsConstructor
public class CalendarUpdaterService {

    private final CalendarRepository calendarRepository;
    private final CalendarDatesRepository calendarDatesRepository;

    private final AgencyService agencyService;

    @Transactional
    @PreAgencyAuthorize
    public Status updateCalendar(String instance, UpdateCalendarRequest updateCalendarRequest) {
        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);
        calendarRepository.findByAgencyAndCalendarName(instance, updateCalendarRequest.getCalendarName()).ifPresent(calendarEntity -> {
            calendarDatesRepository.deleteByAgencyAndCalendar(agencyEntity, calendarEntity);

            CalendarSymbolEntity updatedCalendarSymbolEntity = CalendarEntityMapper.apply(calendarEntity, updateCalendarRequest.getBody(), agencyEntity);
            CalendarSymbolEntity savedCalendarSymbolEntity = calendarRepository.save(updatedCalendarSymbolEntity);

            Set<CalendarDatesEntity> calendarDates = CalendarDatesEntityMapper.apply(updateCalendarRequest.getBody(), savedCalendarSymbolEntity);
            calendarDatesRepository.saveAll(calendarDates);
        });
        return new Status().status(Status.StatusEnum.SUCCESS);
    }
}
