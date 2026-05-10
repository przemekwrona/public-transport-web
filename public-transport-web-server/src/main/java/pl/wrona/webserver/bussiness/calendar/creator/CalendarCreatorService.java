package pl.wrona.webserver.bussiness.calendar.creator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarPayload;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.calendar.CalendarItemCommandService;
import pl.wrona.webserver.bussiness.calendar.CalendarItemQueryService;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.calendar.CalendarDatesEntity;
import pl.wrona.webserver.bussiness.calendar.mapper.CalendarDatesEntityMapper;
import pl.wrona.webserver.core.calendar.CalendarDatesRepository;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;
import pl.wrona.webserver.bussiness.calendar.mapper.CalendarSymbolEntityMapper;
import pl.wrona.webserver.core.calendar.CalendarSymbolQueryRepository;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.Set;

@Service
@AllArgsConstructor
public class CalendarCreatorService {

    private final CalendarSymbolQueryRepository calendarSymbolRepository;
    private final CalendarDatesRepository calendarDatesRepository;
    private final AgencyService agencyService;
    private final CalendarItemCommandService calendarItemCommandService;
    private final CalendarItemQueryService calendarItemQueryService;

    @Transactional
    @PreAgencyAuthorize
    public Status createCalendar(String instance, CalendarPayload calendarPayload) {
        var calendarBody = calendarPayload.getBody();
        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);
        var alreadySavedCalendarItem = calendarItemQueryService.findByAgencyAndStartDateAndEndDate(agencyEntity.getAgencyCode(), calendarBody.getStartDate(), calendarBody.getEndDate());

        var calendarSymbolEntity = CalendarSymbolEntityMapper.apply(calendarBody, agencyEntity);
        calendarSymbolEntity.setCalendarItem(alreadySavedCalendarItem);

        CalendarSymbolEntity savedCalendar = calendarSymbolRepository.save(calendarSymbolEntity);

        Set<CalendarDatesEntity> calendarDates = CalendarDatesEntityMapper.apply(calendarBody, savedCalendar);
        calendarDatesRepository.saveAll(calendarDates);

        return new Status().status(Status.StatusEnum.CREATED);
    }

}
