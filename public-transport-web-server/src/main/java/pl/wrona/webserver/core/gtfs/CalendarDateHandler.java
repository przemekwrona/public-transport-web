package pl.wrona.webserver.core.gtfs;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import pl.wrona.webserver.core.calendar.CalendarDatesEntity;

public class CalendarDateHandler {

    public static ServiceCalendarDate handle(CalendarDatesEntity calendarDate) {
        AgencyAndId agencyAndId = new AgencyAndId();
        agencyAndId.setId(calendarDate.getCalendar().getCalendarName());

        ServiceCalendarDate serviceCalendarDate = new ServiceCalendarDate();
        serviceCalendarDate.setServiceId(agencyAndId);
        serviceCalendarDate.setDate(calendarDate.getServiceDate());
        serviceCalendarDate.setExceptionType(calendarDate.getExceptionType().getGtfsCode());
        return serviceCalendarDate;
    }
}
