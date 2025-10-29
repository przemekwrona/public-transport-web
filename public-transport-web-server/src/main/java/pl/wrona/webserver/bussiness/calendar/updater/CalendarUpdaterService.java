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
import pl.wrona.webserver.core.calendar.CalendarEntity;
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
        calendarRepository.findByAgencyAndCalendarName(agencyEntity, updateCalendarRequest.getCalendarName()).ifPresent(calendarEntity -> {
            calendarDatesRepository.deleteByAgencyAndCalendar(agencyEntity, calendarEntity);

            CalendarEntity updatedCalendarEntity = CalendarEntityMapper.apply(calendarEntity, updateCalendarRequest.getBody(), agencyEntity);
            CalendarEntity savedCalendarEntity = calendarRepository.save(updatedCalendarEntity);

            Set<CalendarDatesEntity> calendarDates = CalendarDatesEntityMapper.apply(updateCalendarRequest.getBody(), savedCalendarEntity);
            calendarDatesRepository.saveAll(calendarDates);
        });
        return new Status().status(Status.StatusEnum.SUCCESS);
    }
}
