package pl.wrona.webserver.bussiness.calendar.creator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CreateCalendarItemRequest;
import org.igeolab.iot.pt.server.api.model.CreateCalendarItemResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.calendar.CalendarItemCommandService;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.calendar.CalendarItemEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class CalendarItemCreatorService {

    private AgencyService agencyService;
    private CalendarItemCommandService calendarItemCommandService;

    @PreAgencyAuthorize
    public CreateCalendarItemResponse createCalendarItem(String instance, CreateCalendarItemRequest createCalendarItemRequest) {
        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);

        var calendarName = "%s--%s--%s".formatted(instance, createCalendarItemRequest.getStartDate(), createCalendarItemRequest.getEndDate());

        var calendarItem = new CalendarItemEntity();
        calendarItem.setAgency(agencyEntity);
        calendarItem.setCalendarName(calendarName);
        calendarItem.setStartDate(createCalendarItemRequest.getStartDate());
        calendarItem.setEndDate(createCalendarItemRequest.getEndDate());

        var savedCalendarItem = calendarItemCommandService.save(calendarItem);

        return new CreateCalendarItemResponse()
                .calendarName(savedCalendarItem.getCalendarName());
    }
}
