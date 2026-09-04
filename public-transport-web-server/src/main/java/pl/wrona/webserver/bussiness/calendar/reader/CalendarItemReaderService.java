package pl.wrona.webserver.bussiness.calendar.reader;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarItemBody;
import org.igeolab.iot.pt.server.api.model.CalendarItemId;
import org.igeolab.iot.pt.server.api.model.GetCalendarItemResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.calendar.CalendarItemQueryService;
import pl.wrona.webserver.bussiness.calendar.symbol.CalendarSymbolQueryService;
import pl.wrona.webserver.bussiness.calendar.mapper.CalendarBodyMapper;
import pl.wrona.webserver.bussiness.calendar.CalendarDatesQueryService;
import pl.wrona.webserver.core.calendar.CalendarItemEntity;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;
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
                            .calendarItemId(new CalendarItemId()
                                    .code(item.getSequenceHex()))
                            .calendarName(item.getCalendarName())
                            .startDate(item.getStartDate())
                            .endDate(item.getEndDate())
                            .symbols(symbols);
                })
                .toList();

        return new GetCalendarItemResponse()
                .items(responseItems);
    }

    @PreAgencyAuthorize
    public CalendarItemBody getCalendarByCalendarCode(String instance, String calendarCode) {
        var calendarItem = calendarItemQueryService.findByAgencyCalendarCode(instance, calendarCode);
        return new CalendarItemBody()
                .calendarItemId(new CalendarItemId()
                        .code(calendarItem.getSequenceHex()))
                .startDate(calendarItem.getStartDate())
                .endDate(calendarItem.getEndDate());
    }
}
