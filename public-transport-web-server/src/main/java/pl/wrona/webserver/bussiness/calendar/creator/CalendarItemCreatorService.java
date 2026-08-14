package pl.wrona.webserver.bussiness.calendar.creator;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CreateCalendarItemRequest;
import org.igeolab.iot.pt.server.api.model.CreateCalendarItemResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.bussiness.calendar.CalendarItemCommandService;
import pl.wrona.webserver.bussiness.calendar.CalendarSequenceQueryService;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.calendar.CalendarItemEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class CalendarItemCreatorService {

    private AgencyService agencyService;
    private CalendarSequenceQueryService calendarSequenceQueryService;
    private CalendarItemCommandService calendarItemCommandService;

    @PreAgencyAuthorize
    @Transactional
    public CreateCalendarItemResponse createCalendarItem(String instance, CreateCalendarItemRequest createCalendarItemRequest) {
        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);
        var nextValue = calendarSequenceQueryService.findByAgencyCode(instance);

        var calendarName = "%s--%s".formatted(createCalendarItemRequest.getStartDate(), createCalendarItemRequest.getEndDate());

        var calendarItem = new CalendarItemEntity();
        calendarItem.setAgency(agencyEntity);
        calendarItem.setCalendarName(calendarName);
        calendarItem.setStartDate(createCalendarItemRequest.getStartDate());
        calendarItem.setEndDate(createCalendarItemRequest.getEndDate());
        calendarItem.setSequence(nextValue);
        calendarItem.setSequenceHex(Hex.toHex(nextValue));
   
        var savedCalendarItem = calendarItemCommandService.save(calendarItem);

        return new CreateCalendarItemResponse()
                .calendarName(savedCalendarItem.getCalendarName());
    }
}
