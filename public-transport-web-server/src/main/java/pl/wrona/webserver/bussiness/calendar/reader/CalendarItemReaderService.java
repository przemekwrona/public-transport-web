package pl.wrona.webserver.bussiness.calendar.reader;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.time.CalendarUtils;
import org.igeolab.iot.pt.server.api.model.CalendarItemBody;
import org.igeolab.iot.pt.server.api.model.CalendarSymbolBody;
import org.igeolab.iot.pt.server.api.model.GetCalendarItemResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.calendar.CalendarItemQueryService;
import pl.wrona.webserver.bussiness.calendar.CalendarSymbolQueryService;
import pl.wrona.webserver.bussiness.calendar.mapper.CalendarBodyMapper;
import pl.wrona.webserver.bussiness.calendar.mapper.CalendarSymbolEntityMapper;
import pl.wrona.webserver.core.calendar.CalendarDatesEntity;
import pl.wrona.webserver.core.calendar.CalendarDatesQueryService;
import pl.wrona.webserver.core.calendar.CalendarItemEntity;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CalendarItemReaderService {

    private final CalendarItemQueryService calendarItemQueryService;
    private final CalendarSymbolQueryService calendarSymbolQueryService;
    private final CalendarDatesQueryService calendarDatesQueryService;

    @PreAgencyAuthorize
    public GetCalendarItemResponse getCalendarItems(String instance) {
        var items = calendarItemQueryService.findByStartDateAndEndDate(instance);

        var calendarNames = items.stream().map(CalendarItemEntity::getCalendarName).toList();

        var symbolEntities = calendarSymbolQueryService.findAllByAgencyAndCalendarNamesIn(instance, calendarNames);
        var symbolDictionary = symbolEntities.stream()
                .collect(Collectors.groupingBy(CalendarSymbolEntity::getCalendarItem));

        var symbolDateEntities = calendarDatesQueryService.findAllByCalendar(symbolEntities);
        var dateDictionary = symbolDateEntities.stream()
                .collect(Collectors.groupingBy(entity -> entity.getCalendar().getServiceId()));

        var responseItems = items.stream()
                .map(item -> {
                    var symbols = symbolDictionary.getOrDefault(item, List.of()).stream()
                            .map(it -> CalendarBodyMapper.applySymbol(item, it, dateDictionary))
                            .toList();

                    return new CalendarItemBody()
                            .calendarName(item.getCalendarName())
                            .startDate(item.getStartDate())
                            .endDate(item.getEndDate())
                            .symbols(symbols);
                })
                .toList();

        return new GetCalendarItemResponse()
                .items(responseItems);
    }
}
