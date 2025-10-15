package pl.wrona.webserver.core.calendar;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.AgencyService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CalendarService {

    private final AgencyService agencyService;

    private final CalendarRepository calendarRepository;

    @Deprecated
    public Optional<CalendarEntity> findCalendarByCalendarName(String calendarName) {
        return calendarRepository.findByAgencyAndCalendarName(agencyService.getLoggedAgency(), calendarName);
    }

    @Deprecated
    public List<CalendarEntity> findActiveCalendar() {
        LocalDate now = LocalDate.now();
        return this.calendarRepository.findAllByAgencyAndStartDateBeforeAndEndDateAfter(agencyService.getLoggedAgency(), now, now);
    }


}
