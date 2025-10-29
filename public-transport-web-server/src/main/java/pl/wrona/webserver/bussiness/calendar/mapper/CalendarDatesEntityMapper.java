package pl.wrona.webserver.bussiness.calendar.mapper;

import org.igeolab.iot.pt.server.api.model.CalendarBody;
import pl.wrona.webserver.bussiness.trip.core.calendar.CalendarDatesEntity;
import pl.wrona.webserver.bussiness.trip.core.calendar.CalendarDatesId;
import pl.wrona.webserver.bussiness.trip.core.calendar.CalendarEntity;
import pl.wrona.webserver.bussiness.trip.core.calendar.ExceptionType;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CalendarDatesEntityMapper {

    public static Set<CalendarDatesEntity> apply(CalendarBody calendarBody, CalendarEntity savedCalendar) {
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

        return Stream.concat(calendarDatesIncluded.stream(), calendarDatesExcluded.stream()).collect(Collectors.toSet());
    }

}
