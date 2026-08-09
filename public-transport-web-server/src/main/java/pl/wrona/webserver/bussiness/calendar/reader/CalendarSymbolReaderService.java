package pl.wrona.webserver.bussiness.calendar.reader;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarItemBody;
import org.igeolab.iot.pt.server.api.model.CalendarItemId1;
import org.igeolab.iot.pt.server.api.model.CalendarSymbolBody;
import org.igeolab.iot.pt.server.api.model.GetCalendarItemResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.calendar.CalendarItemQueryService;
import pl.wrona.webserver.bussiness.calendar.CalendarSymbolQueryService;
import pl.wrona.webserver.bussiness.calendar.mapper.CalendarBodyMapper;
import pl.wrona.webserver.bussiness.calendar.CalendarDatesQueryService;
import pl.wrona.webserver.core.calendar.CalendarDatesEntity;
import pl.wrona.webserver.core.calendar.CalendarItemEntity;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;
import java.util.Map;
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
        var calendarSymbolEntity =  calendarSymbolQueryService.findByAgencyAndCalendarAndSymbol(instance, calendarCode, calendarSymbol);
        var calendarDatesEntity = calendarDatesQueryService.findAllByCalendar(calendarSymbolEntity);
        var calendarDateDictionary = calendarDatesEntity.stream()
                .collect(Collectors.groupingBy(it -> it.getCalendar().getServiceId()));

        return CalendarBodyMapper.applySymbol(calendarItemEntity, calendarSymbolEntity, calendarDateDictionary);
    }
}
