package pl.wrona.webserver.bussiness.gtfs.download;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;

public class CalendarHandler {

    public static ServiceCalendar handle(CalendarSymbolEntity calendar) {
        AgencyAndId agencyAndId = new AgencyAndId();

        ServiceCalendar serviceCalendar = new ServiceCalendar();
        serviceCalendar.setServiceId(agencyAndId);
        serviceCalendar.setMonday(calendar.isMonday() ? 1 : 0);
        serviceCalendar.setTuesday(calendar.isTuesday() ? 1 : 0);
        serviceCalendar.setWednesday(calendar.isWednesday() ? 1 : 0);
        serviceCalendar.setThursday(calendar.isThursday() ? 1 : 0);
        serviceCalendar.setFriday(calendar.isFriday() ? 1 : 0);
        serviceCalendar.setSaturday(calendar.isSaturday() ? 1 : 0);
        serviceCalendar.setSunday(calendar.isSunday() ? 1 : 0);

        return serviceCalendar;

    }
}
