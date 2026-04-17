package pl.wrona.webserver.core.calendar;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.AgencyService;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CalendarSymbolQueryService {

    private final AgencyService agencyService;

    private final CalendarSymbolRepository calendarSymbolRepository;

    @Deprecated
    public Optional<CalendarSymbolEntity> findCalendarByCalendarName(String calendarName) {
        return calendarSymbolRepository.findByAgencyAndCalendarName(agencyService.getLoggedAgency().getAgencyCode(), calendarName);
    }

    public CalendarSymbolEntity findCalendarByCalendarCode(CalendarItemEntity itemEntity, String symbol) {
        return calendarSymbolRepository.findByCalendarItemAndDesignationEquals(itemEntity, symbol);
    }

}
