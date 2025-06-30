package pl.wrona.webserver.core.calendar;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarBody;
import org.igeolab.iot.pt.server.api.model.CalendarPayload;
import org.igeolab.iot.pt.server.api.model.CalendarQuery;
import org.igeolab.iot.pt.server.api.model.GetCalendarsResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.UpdateCalendarRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.AgencyService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class CalendarService {

    private final AgencyService agencyService;
    private final CalendarDatesService calendarDatesService;

    private final CalendarRepository calendarRepository;
    private final CalendarDatesRepository calendarDatesRepository;

    @Transactional
    public Status createCalendar(CalendarPayload calendarPayload) {
        var calendarBody = calendarPayload.getBody();

        var calendarIdPrefix = "%s/%s/%s".formatted(calendarBody.getDesignation(),
                calendarBody.getStartDate().format(DateTimeFormatter.BASIC_ISO_DATE),
                calendarBody.getEndDate().format(DateTimeFormatter.BASIC_ISO_DATE));

        var lastSavedCalendarNumber = this.findCalendarStartsWith(calendarIdPrefix).stream()
                .map(CalendarEntity::getCalendarName)
                .map(calendarName -> calendarName.substring(calendarIdPrefix.length() + 1))
                .map(Long::valueOf).max(Comparator.naturalOrder())
                .orElse(0L);

        var calendarEntity = CalendarEntityMapper.apply(calendarBody, agencyService.getLoggedAgency());
        calendarEntity.setCalendarName("%s/%s".formatted(calendarIdPrefix, lastSavedCalendarNumber + 1));
        CalendarEntity savedCalendar = calendarRepository.save(calendarEntity);

        Set<CalendarDatesEntity> calendarDates = CalendarDatesEntityMapper.apply(calendarBody, savedCalendar);
        calendarDatesRepository.saveAll(calendarDates);

        return new Status().status(Status.StatusEnum.CREATED);
    }


    public GetCalendarsResponse getCalendars() {
        Map<Long, List<CalendarDatesEntity>> calendarDatesDictionary = calendarDatesService.mapAllByAgency();

        var calendars = calendarRepository.findAllByAgency(agencyService.getLoggedAgency()).stream()
                .map(calendar -> CalendarBodyMapper.apply(calendar, calendarDatesDictionary))
                .toList();

        return new GetCalendarsResponse()
                .calendars(calendars);
    }

    public Optional<CalendarEntity> findCalendarByCalendarName(String calendarName) {
        return calendarRepository.findByAgencyAndCalendarName(agencyService.getLoggedAgency(), calendarName);
    }

    @Transactional
    public Status deleteCalendarByCalendarName(CalendarQuery calendarQuery) {
        calendarRepository.findByAgencyAndCalendarName(agencyService.getLoggedAgency(), calendarQuery.getCalendarName())
                .ifPresent((CalendarEntity calendarEntity) -> {
                    calendarDatesRepository.deleteByAgencyAndCalendar(agencyService.getLoggedAgency(), calendarEntity);
                    calendarRepository.delete(calendarEntity);
                });

        return new Status().status(Status.StatusEnum.DELETED);
    }

    public CalendarBody getCalendarByCalendarName(CalendarQuery calendarQuery) {
        Map<Long, List<CalendarDatesEntity>> calendarDatesDictionary = calendarDatesService.mapAllByAgency();

        return calendarRepository.findByAgencyAndCalendarName(agencyService.getLoggedAgency(), calendarQuery.getCalendarName())
                .map(calendar -> CalendarBodyMapper.apply(calendar, calendarDatesDictionary))
                .orElseThrow();
    }

    @Transactional
    public Status updateCalendar(UpdateCalendarRequest updateCalendarRequest) {
        calendarRepository.findByAgencyAndCalendarName(agencyService.getLoggedAgency(), updateCalendarRequest.getCalendarName()).ifPresent(calendarEntity -> {
            calendarDatesRepository.deleteByAgencyAndCalendar(agencyService.getLoggedAgency(), calendarEntity);

            CalendarEntity updatedCalendarEntity = CalendarEntityMapper.apply(calendarEntity, updateCalendarRequest.getBody(), agencyService.getLoggedAgency());
            CalendarEntity savedCalendarEntity = calendarRepository.save(updatedCalendarEntity);

            Set<CalendarDatesEntity> calendarDates = CalendarDatesEntityMapper.apply(updateCalendarRequest.getBody(), savedCalendarEntity);
            calendarDatesRepository.saveAll(calendarDates);
        });
        return new Status().status(Status.StatusEnum.SUCCESS);
    }

    public List<CalendarEntity> findCalendarStartsWith(String calendarName) {
        return this.calendarRepository.findAllByAgencyAndCalendarNameStartingWith(agencyService.getLoggedAgency(), calendarName);
    }

    public List<CalendarEntity> findActiveCalendar() {
        LocalDate now = LocalDate.now();
        return this.calendarRepository.findAllByAgencyAndStartDateBeforeAndEndDateAfter(agencyService.getLoggedAgency(), now, now);
    }


}
