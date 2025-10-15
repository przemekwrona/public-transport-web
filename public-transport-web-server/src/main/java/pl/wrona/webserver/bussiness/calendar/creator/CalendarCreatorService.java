package pl.wrona.webserver.bussiness.calendar.creator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarPayload;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.calendar.CalendarDatesEntity;
import pl.wrona.webserver.bussiness.calendar.mapper.CalendarDatesEntityMapper;
import pl.wrona.webserver.core.calendar.CalendarDatesRepository;
import pl.wrona.webserver.core.calendar.CalendarEntity;
import pl.wrona.webserver.bussiness.calendar.mapper.CalendarEntityMapper;
import pl.wrona.webserver.core.calendar.CalendarRepository;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class CalendarCreatorService {

    private final CalendarRepository calendarRepository;
    private final CalendarDatesRepository calendarDatesRepository;
    private final AgencyService agencyService;

    @Transactional
    @PreAgencyAuthorize
    public Status createCalendar(String instance, CalendarPayload calendarPayload) {
        var calendarBody = calendarPayload.getBody();
        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);

        var calendarIdPrefix = "%s/%s/%s".formatted(calendarBody.getDesignation(),
                calendarBody.getStartDate().format(DateTimeFormatter.BASIC_ISO_DATE),
                calendarBody.getEndDate().format(DateTimeFormatter.BASIC_ISO_DATE));

        var lastSavedCalendarNumber = this.findCalendarStartsWith(agencyEntity, calendarIdPrefix).stream()
                .map(CalendarEntity::getCalendarName)
                .map(calendarName -> calendarName.substring(calendarIdPrefix.length() + 1))
                .map(Long::valueOf).max(Comparator.naturalOrder())
                .orElse(0L);

        var calendarEntity = CalendarEntityMapper.apply(calendarBody, agencyEntity);
        calendarEntity.setCalendarName("%s/%s".formatted(calendarIdPrefix, lastSavedCalendarNumber + 1));
        CalendarEntity savedCalendar = calendarRepository.save(calendarEntity);

        Set<CalendarDatesEntity> calendarDates = CalendarDatesEntityMapper.apply(calendarBody, savedCalendar);
        calendarDatesRepository.saveAll(calendarDates);

        return new Status().status(Status.StatusEnum.CREATED);
    }

    public List<CalendarEntity> findCalendarStartsWith(AgencyEntity agencyEntity, String calendarName) {
        return this.calendarRepository.findAllByAgencyAndCalendarNameStartingWith(agencyEntity, calendarName);
    }

}
