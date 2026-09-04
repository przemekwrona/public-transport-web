package pl.wrona.webserver.bussiness.calendar;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.core.calendar.CalendarItemEntity;
import pl.wrona.webserver.core.calendar.CalendarItemQueryRepository;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class CalendarItemQueryService {

    private final CalendarItemQueryRepository calendarItemQueryRepository;

    public List<CalendarItemEntity> findByStartDateAndEndDate(String instance) {
        return calendarItemQueryRepository.findByAgency(instance);
    }

    public CalendarItemEntity findByAgencyCalendarCode(String instance, String calendarCode) {
        return calendarItemQueryRepository.findByAgencyAndSequenceHexEquals(instance, Hex.fromHex(calendarCode));
    }

}
