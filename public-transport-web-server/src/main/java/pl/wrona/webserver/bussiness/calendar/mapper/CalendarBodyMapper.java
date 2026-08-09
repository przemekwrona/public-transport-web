package pl.wrona.webserver.bussiness.calendar.mapper;

import org.igeolab.iot.pt.server.api.model.CalendarBody;
import org.igeolab.iot.pt.server.api.model.CalendarId;
import org.igeolab.iot.pt.server.api.model.CalendarItemId1;
import org.igeolab.iot.pt.server.api.model.CalendarSymbolBody;
import org.igeolab.iot.pt.server.api.model.CalendarSymbolId;
import pl.wrona.webserver.core.calendar.CalendarDatesEntity;
import pl.wrona.webserver.core.calendar.CalendarItemEntity;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;
import pl.wrona.webserver.core.calendar.ExceptionType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class CalendarBodyMapper {

    public static CalendarBody apply(CalendarItemEntity item, CalendarSymbolEntity symbol, Map<Long, List<CalendarDatesEntity>> calendarDatesDictionary) {
        List<LocalDate> included = calendarDatesDictionary.getOrDefault(symbol.getServiceId(), List.of()).stream()
                .filter(calendarDate -> ExceptionType.ADDED.equals(calendarDate.getExceptionType()))
                .map(cd -> cd.getCalendarDatesId().getDate()).toList();

        List<LocalDate> excluded = calendarDatesDictionary.getOrDefault(symbol.getServiceId(), List.of()).stream()
                .filter(calendarDate -> ExceptionType.REMOVED.equals(calendarDate.getExceptionType()))
                .map(cd -> cd.getCalendarDatesId().getDate()).toList();

        return new CalendarBody()
                .calendarName(item.getCalendarName())
                .calendarId(new CalendarSymbolId()
                        .calendarItemId(new CalendarItemId1()
                                .code(item.getSequenceHex()))
                        .symbol(symbol.getDesignation()))
                .designation(symbol.getDesignation())
                .description(symbol.getDescription())
                .startDate(item.getStartDate())
                .endDate(item.getEndDate())
                .monday(symbol.isMonday())
                .tuesday(symbol.isTuesday())
                .wednesday(symbol.isWednesday())
                .thursday(symbol.isThursday())
                .friday(symbol.isFriday())
                .saturday(symbol.isSaturday())
                .sunday(symbol.isSunday())
                .included(included)
                .excluded(excluded);
    }

    public static CalendarSymbolBody applySymbol(CalendarItemEntity item, CalendarSymbolEntity calendar, Map<Long, List<CalendarDatesEntity>> calendarDatesDictionary) {
        List<LocalDate> included = calendarDatesDictionary.getOrDefault(calendar.getServiceId(), List.of()).stream()
                .filter(calendarDate -> ExceptionType.ADDED.equals(calendarDate.getExceptionType()))
                .map(cd -> cd.getCalendarDatesId().getDate()).toList();

        List<LocalDate> excluded = calendarDatesDictionary.getOrDefault(calendar.getServiceId(), List.of()).stream()
                .filter(calendarDate -> ExceptionType.REMOVED.equals(calendarDate.getExceptionType()))
                .map(cd -> cd.getCalendarDatesId().getDate()).toList();

        return new CalendarSymbolBody()
                .calendarId(new CalendarId()
                        .name(item.getSequenceHex())
                        .version(1))
                .calendarName(item.getCalendarName())
                .designation(calendar.getDesignation())
                .description(calendar.getDescription())
                .startDate(item.getStartDate())
                .endDate(item.getEndDate())
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
