package pl.wrona.webserver.bussiness.calendar.mapper;

import org.igeolab.iot.pt.server.api.model.CalendarBody;
import pl.wrona.webserver.core.calendar.CalendarDatesEntity;
import pl.wrona.webserver.core.calendar.CalendarItemEntity;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;
import pl.wrona.webserver.core.calendar.ExceptionType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class CalendarBodyMapper {

    public static CalendarBody apply(CalendarItemEntity item, CalendarSymbolEntity calendar, Map<Long, List<CalendarDatesEntity>> calendarDatesDictionary) {
        List<LocalDate> included = calendarDatesDictionary.getOrDefault(calendar.getServiceId(), List.of()).stream()
                .filter(calendarDate -> ExceptionType.ADDED.equals(calendarDate.getExceptionType()))
                .map(cd -> cd.getCalendarDatesId().getDate()).toList();

        List<LocalDate> excluded = calendarDatesDictionary.getOrDefault(calendar.getServiceId(), List.of()).stream()
                .filter(calendarDate -> ExceptionType.REMOVED.equals(calendarDate.getExceptionType()))
                .map(cd -> cd.getCalendarDatesId().getDate()).toList();

        return new CalendarBody()
                .calendarName(calendar.getCalendarItem().getCalendarName())
                .designation(calendar.getDesignation())
                .description(calendar.getDescription())
                .startDate(calendar.getCalendarItem().getStartDate())
                .endDate(calendar.getCalendarItem().getEndDate())
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
