package pl.wrona.webserver.bussiness.calendar.reader;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarItemBody;
import org.igeolab.iot.pt.server.api.model.GetCalendarItemResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.calendar.CalendarItemQueryService;
import pl.wrona.webserver.core.calendar.CalendarItemEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class CalendarItemReaderService {

    private final CalendarItemQueryService calendarItemQueryService;

    @PreAgencyAuthorize
    public GetCalendarItemResponse getCalendarItems(String instance) {
        var items = calendarItemQueryService.findByStartDateAndEndDate(instance);

        var calendarNames = items.stream().map(CalendarItemEntity::getCalendarName).toList();

        var responseItems = items.stream()
                .map(item -> new CalendarItemBody()
                        .calendarName(item.getCalendarName())
                        .startDate(item.getStartDate())
                        .endDate(item.getEndDate()))
                .toList();

        return new GetCalendarItemResponse()
                .items(responseItems);
    }
}
