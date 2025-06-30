package pl.wrona.webserver.core.calendar;

import org.igeolab.iot.pt.server.api.model.CalendarBody;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class CalendarBodyMapper {

    public static CalendarBody apply(CalendarEntity calendar, Map<Long, List<CalendarDatesEntity>> calendarDatesDictionary) {
        List<LocalDate> included = calendarDatesDictionary.getOrDefault(calendar.getServiceId(), List.of()).stream()
                .filter(calendarDate -> ExceptionType.ADDED.equals(calendarDate.getExceptionType()))
                .map(cd -> cd.getCalendarDatesId().getDate()).toList();

        List<LocalDate> excluded = calendarDatesDictionary.getOrDefault(calendar.getServiceId(), List.of()).stream()
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
    }

}
