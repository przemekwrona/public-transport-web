package pl.wrona.webserver.agency.calendar;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarBody;
import org.igeolab.iot.pt.server.api.model.CalendarPayload;
import org.igeolab.iot.pt.server.api.model.GetCalendarsResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.agency.AgencyService;

import java.time.LocalDate;
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
                .map(calendar -> {
                    List<LocalDate> included = calendarDatesDictionary.get(calendar.getServiceId()).stream()
                            .filter(calendarDate -> ExceptionType.ADDED.equals(calendarDate.getExceptionType()))
                            .map(cd -> cd.getCalendarDatesId().getDate()).toList();

                    List<LocalDate> excluded = calendarDatesDictionary.get(calendar.getServiceId()).stream()
                            .filter(calendarDate -> ExceptionType.REMOVED.equals(calendarDate.getExceptionType()))
                            .map(cd -> cd.getCalendarDatesId().getDate()).toList();

                    return new CalendarBody()
                            .calendarName(calendar.getCalendarName())
                            .designation(calendar.getDesignation())
                            .description(calendar.getDescription())
                            .startDate(calendar.getStartDate())
                            .endDate(calendar.getEndDate())
                            .monday(calendar.isMonday())
                            .tuesday(calendar.isTuesday())
                            .wednesday(calendar.isWednesday())
                            .thursday(calendar.isThursday())
                            .friday(calendar.isFriday())
                            .saturday(calendar.isSaturday())
                            .sunday(calendar.isSunday())
                            .included(included)
                            .excluded(excluded);
                })
                .toList();

        return new GetCalendarsResponse()
                .calendars(calendars);
    }
}
