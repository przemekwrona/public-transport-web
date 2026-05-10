package pl.wrona.webserver.bussiness.calendar;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.calendar.CalendarItemEntity;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;
import pl.wrona.webserver.core.calendar.CalendarSymbolQueryRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CalendarSymbolQueryService {

    private final AgencyService agencyService;

    private final CalendarSymbolQueryRepository calendarSymbolQueryRepository;

    @Deprecated
    public Optional<CalendarSymbolEntity> findCalendarByCalendarName(String calendarName) {
        return calendarSymbolQueryRepository.findByAgencyAndCalendarName(agencyService.getLoggedAgency().getAgencyCode(), calendarName);
    }

    public CalendarSymbolEntity findCalendarByCalendarCode(CalendarItemEntity itemEntity, String symbol) {
        return calendarSymbolQueryRepository.findByCalendarItemAndDesignationEquals(itemEntity, symbol);
    }

    public List<CalendarSymbolEntity> findAllByAgencyAndCalendarNamesIn(String agencyCode, List<String> calendarNames) {
        return calendarSymbolQueryRepository.findAllByAgencyAndCalendarNamesIn(agencyCode, calendarNames);
    }

}
