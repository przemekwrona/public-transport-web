package pl.wrona.webserver.core.calendar;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.AgencyService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CalendarDatesService {

    private final AgencyService agencyService;
    private final CalendarDatesRepository calendarDatesRepository;

    public Map<Long, List<CalendarDatesEntity>> mapAllByAgency() {
        return calendarDatesRepository.findAllByAgency(agencyService.getLoggedAgency()).stream()
                .collect(Collectors.groupingBy(calendarDates -> calendarDates.getCalendarDatesId().getServiceId()));
    }

    public List<CalendarDatesEntity> findAllActiveCalendarDate() {
        LocalDate now = LocalDate.now();
        return calendarDatesRepository.findActiveCalendarDate(agencyService.getLoggedAgency(), now, now);
    }
}
