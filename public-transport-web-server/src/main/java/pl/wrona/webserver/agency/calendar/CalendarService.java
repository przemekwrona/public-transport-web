package pl.wrona.webserver.agency.calendar;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarPayload;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.agency.AgencyRepository;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;
    private final CalendarDatesRepository calendarDatesRepository;

    @Transactional
    public Status createCalendar(CalendarPayload calendarPayload) {
        var calendarBody = calendarPayload.getBody();

        var calendarEntity = new CalendarEntity();

        calendarEntity.setCalendarName(calendarBody.getName());
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
                        .exceptionType("0")
                        .calendar(savedCalendar)
                        .build())
                .collect(Collectors.toSet());

        Set<CalendarDatesEntity> calendarDatesExcluded = calendarBody.getIncluded().stream()
                .map(excludedDate -> CalendarDatesEntity.builder()
                        .calendarDatesId(CalendarDatesId.builder()
                                .serviceId(savedCalendar.getServiceId())
                                .date(excludedDate)
                                .build())
                        .exceptionType("1")
                        .calendar(savedCalendar)
                        .build())
                .collect(Collectors.toSet());

        Set<CalendarDatesEntity> calendarDates = Stream.concat(calendarDatesIncluded.stream(), calendarDatesExcluded.stream()).collect(Collectors.toSet());
        calendarDatesRepository.saveAll(calendarDates);

        return new Status().status(Status.StatusEnum.CREATED);
    }
}
