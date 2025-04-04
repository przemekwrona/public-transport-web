package pl.wrona.webserver.agency.calendar;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarBody;
import org.igeolab.iot.pt.server.api.model.CalendarPayload;
import org.igeolab.iot.pt.server.api.model.CalendarQuery;
import org.igeolab.iot.pt.server.api.model.GetCalendarsResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.agency.AgencyService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        var calendarEntity = new CalendarEntity();
        calendarEntity.setAgency(agencyService.getLoggedAgency());

        calendarEntity.setCalendarName(calendarBody.getCalendarName());
        calendarEntity.setDesignation(calendarBody.getDesignation());
        calendarEntity.setDescription(calendarBody.getDescription());

        calendarEntity.setMonday(calendarBody.getMonday());
        calendarEntity.setThursday(calendarBody.getMonday());
        calendarEntity.setMonday(calendarBody.getMonday());
        calendarEntity.setTuesday(calendarBody.getTuesday());
        calendarEntity.setWednesday(calendarBody.getWednesday());
        calendarEntity.setThursday(calendarBody.getThursday());
        calendarEntity.setFriday(calendarBody.getFriday());
        calendarEntity.setSaturday(calendarBody.getSaturday());
        calendarEntity.setSunday(calendarBody.getSunday());

        calendarEntity.setStartDate(calendarBody.getStartDate());
        calendarEntity.setEndDate(calendarBody.getEndDate());

        CalendarEntity savedCalendar = calendarRepository.save(calendarEntity);

        Set<CalendarDatesEntity> calendarDatesIncluded = calendarBody.getIncluded().stream()
                .map(includeDate -> CalendarDatesEntity.builder()
                        .calendarDatesId(CalendarDatesId.builder()
                                .serviceId(savedCalendar.getServiceId())
                                .date(includeDate)
                                .build())
                        .exceptionType(ExceptionType.ADDED)
                        .calendar(savedCalendar)
                        .build())
                .collect(Collectors.toSet());

        Set<CalendarDatesEntity> calendarDatesExcluded = calendarBody.getExcluded().stream()
                .map(excludedDate -> CalendarDatesEntity.builder()
                        .calendarDatesId(CalendarDatesId.builder()
                                .serviceId(savedCalendar.getServiceId())
                                .date(excludedDate)
                                .build())
                        .exceptionType(ExceptionType.REMOVED)
                        .calendar(savedCalendar)
                        .build())
                .collect(Collectors.toSet());

        Set<CalendarDatesEntity> calendarDates = Stream.concat(calendarDatesIncluded.stream(), calendarDatesExcluded.stream()).collect(Collectors.toSet());
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
}
