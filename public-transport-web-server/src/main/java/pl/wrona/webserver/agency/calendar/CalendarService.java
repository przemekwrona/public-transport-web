package pl.wrona.webserver.agency.calendar;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarBody;
import org.igeolab.iot.pt.server.api.model.CalendarPayload;
import org.igeolab.iot.pt.server.api.model.CalendarQuery;
import org.igeolab.iot.pt.server.api.model.GetCalendarsResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.UpdateCalendarRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.agency.AgencyService;
import pl.wrona.webserver.exception.BusinessException;

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

        var existsByCalendarName = this.existsByCalendarName(calendarBody.getCalendarName());

        if (existsByCalendarName) {
            throw new BusinessException("1000", "Calendar with name %s already exists. Select another one.".formatted(calendarBody.getCalendarName()));
        }

        var calendarEntity = CalendarEntityMapper.apply(calendarBody, agencyService.getLoggedAgency());
        CalendarEntity savedCalendar = calendarRepository.save(calendarEntity);

        Set<CalendarDatesEntity> calendarDates = CalendarDatesEntityMapper.apply(calendarBody, savedCalendar);
        calendarDatesRepository.saveAll(calendarDates);

        return new Status().status(Status.StatusEnum.CREATED);
    }


    public GetCalendarsResponse getCalendars() {
        Map<Long, List<CalendarDatesEntity>> calendarDatesDictionary = calendarDatesService.findAllByAgency(agencyService.getLoggedAgency());

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
        Map<Long, List<CalendarDatesEntity>> calendarDatesDictionary = calendarDatesService.findAllByAgency(agencyService.getLoggedAgency());

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

    public boolean existsByCalendarName(String calendarName) {
        return this.calendarRepository.existsByAgencyAndCalendarName(agencyService.getLoggedAgency(), calendarName);
    }
}
