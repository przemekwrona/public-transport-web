package pl.wrona.webserver.bussiness.calendar.deletion;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.DeleteCalendarItemRequest;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.calendar.CalendarItemCommandService;
import pl.wrona.webserver.bussiness.calendar.CalendarItemQueryService;
import pl.wrona.webserver.bussiness.calendar.CalendarSymbolQueryService;
import pl.wrona.webserver.exception.BusinessException;

@Service
@AllArgsConstructor
public class CalendarItemDeletionService {

    private final CalendarItemQueryService calendarItemQueryService;
    private final CalendarItemCommandService calendarItemCommandService;

    private final CalendarSymbolQueryService calendarSymbolQueryService;

    public Status deleteCalendarItem(String instance, DeleteCalendarItemRequest deleteCalendarItemRequest) {
        var calendarSymbols = calendarSymbolQueryService.findAllByAgencyAndCalendarItemId(instance, deleteCalendarItemRequest.getCalendarItemId());

        if (calendarSymbols.isEmpty()) {
            var calendarItem = calendarItemQueryService.findByAgencyAndStartDateAndEndDate(instance, deleteCalendarItemRequest.getCalendarItemId().getStartDate(), deleteCalendarItemRequest.getCalendarItemId().getEndDate());
            calendarItemCommandService.deleteCalendarItem(calendarItem);
            return new Status().status(Status.StatusEnum.DELETED);
        } else {
            throw new BusinessException("ERROR:202605142327", "Can not delete calendar item, first delete symbols");
        }
    }

}
