package pl.wrona.webserver.core.calendar;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarBody;
import org.igeolab.iot.pt.server.api.model.CalendarQuery;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.AgencyService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CalendarService {

    private final AgencyService agencyService;
    private final CalendarDatesService calendarDatesService;

    private final CalendarRepository calendarRepository;

    public Optional<CalendarEntity> findCalendarByCalendarName(String calendarName) {
        return calendarRepository.findByAgencyAndCalendarName(agencyService.getLoggedAgency(), calendarName);
    }

    public CalendarBody getCalendarByCalendarName(CalendarQuery calendarQuery) {
        Map<Long, List<CalendarDatesEntity>> calendarDatesDictionary = calendarDatesService.mapAllByAgency();

        return calendarRepository.findByAgencyAndCalendarName(agencyService.getLoggedAgency(), calendarQuery.getCalendarName())
                .map(calendar -> CalendarBodyMapper.apply(calendar, calendarDatesDictionary))
                .orElseThrow();
    }

    public List<CalendarEntity> findActiveCalendar() {
        LocalDate now = LocalDate.now();
        return this.calendarRepository.findAllByAgencyAndStartDateBeforeAndEndDateAfter(agencyService.getLoggedAgency(), now, now);
    }


}
