package pl.wrona.webserver.core.calendar;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CalendarDatesQueryService {

    private final CalendarDatesRepository calendarDatesRepository;

    public List<CalendarDatesEntity> findAllByCalendar(CalendarSymbolEntity calendarId) {
        return calendarDatesRepository.findAllByCalendar(calendarId);
    }

    public List<CalendarDatesEntity> findAllByCalendar(List<CalendarSymbolEntity> calendarSymbols) {
        return calendarDatesRepository.findAllByCalendarIn(calendarSymbols);
    }
}
