package pl.wrona.webserver.agency.calendar;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.agency.entity.Agency;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CalendarDatesService {

    private final CalendarDatesRepository calendarDatesRepository;

    public Map<Long, List<CalendarDatesEntity>> mapAllByAgency(Agency agency) {
        return calendarDatesRepository.findAllByAgency(agency).stream()
                .collect(Collectors.groupingBy(calendarDates -> calendarDates.getCalendarDatesId().getServiceId()));
    }
}
