package pl.wrona.webserver.bussiness.calendar;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.calendar.CalendarItemEntity;
import pl.wrona.webserver.core.calendar.CalendarItemQueryRepository;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class CalendarItemQueryService {

    private final CalendarItemQueryRepository calendarItemQueryRepository;

    @PreAgencyAuthorize
    public CalendarItemEntity findByAgencyAndStartDateAndEndDate(String instance, LocalDate startDate, LocalDate endDate) {
        return calendarItemQueryRepository.findByAgencyAndStartDateAndEndDate(instance, startDate, endDate);
    }

}
