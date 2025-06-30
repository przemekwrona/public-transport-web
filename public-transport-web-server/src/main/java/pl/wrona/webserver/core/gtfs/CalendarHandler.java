package pl.wrona.webserver.core.gtfs;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import pl.wrona.webserver.core.calendar.CalendarEntity;

public class CalendarHandler {

    public static ServiceCalendar handle(CalendarEntity calendar) {
        AgencyAndId agencyAndId = new AgencyAndId();
        agencyAndId.setId(calendar.getCalendarName());

        ServiceCalendar serviceCalendar = new ServiceCalendar();
        serviceCalendar.setServiceId(agencyAndId);
        serviceCalendar.setMonday(calendar.isMonday() ? 1 : 0);
        serviceCalendar.setTuesday(calendar.isTuesday() ? 1 : 0);
        serviceCalendar.setWednesday(calendar.isWednesday() ? 1 : 0);
        serviceCalendar.setThursday(calendar.isThursday() ? 1 : 0);
        serviceCalendar.setFriday(calendar.isFriday() ? 1 : 0);
        serviceCalendar.setSaturday(calendar.isSaturday() ? 1 : 0);
        serviceCalendar.setSunday(calendar.isSunday() ? 1 : 0);

        serviceCalendar.setStartDate(calendar.getServiceStartDate());
        serviceCalendar.setEndDate(calendar.getServiceEndDate());

        return serviceCalendar;

    }
}
