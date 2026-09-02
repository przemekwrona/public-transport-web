package pl.wrona.webserver.bussiness.calendar.reader;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarSymbolBody;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.calendar.CalendarItemQueryService;
import pl.wrona.webserver.bussiness.calendar.CalendarSymbolQueryService;
import pl.wrona.webserver.bussiness.calendar.mapper.CalendarBodyMapper;
import pl.wrona.webserver.bussiness.calendar.CalendarDatesQueryService;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CalendarSymbolReaderService {

    private final CalendarItemQueryService calendarItemQueryService;
    private final CalendarSymbolQueryService calendarSymbolQueryService;
    private final CalendarDatesQueryService calendarDatesQueryService;

    @PreAgencyAuthorize
    public CalendarSymbolBody getCalendarSymbol(String instance, String calendarCode, String calendarSymbol) {
        var calendarItemEntity =  calendarItemQueryService.findByAgencyCalendarCode(instance, calendarCode);
        var calendarSymbolEntity =  calendarSymbolQueryService.findByAgencyAndBrigadeAndCalendarAndSymbol(instance, calendarCode, calendarSymbol);
        var calendarDatesEntity = calendarDatesQueryService.findAllByCalendar(calendarSymbolEntity);
        var calendarDateDictionary = calendarDatesEntity.stream()
                .collect(Collectors.groupingBy(it -> it.getCalendar().getServiceId()));

        return CalendarBodyMapper.applySymbol(calendarItemEntity, calendarSymbolEntity, calendarDateDictionary);
    }
}
