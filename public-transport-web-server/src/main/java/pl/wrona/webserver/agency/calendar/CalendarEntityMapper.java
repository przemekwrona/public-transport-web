package pl.wrona.webserver.agency.calendar;

import org.igeolab.iot.pt.server.api.model.CalendarBody;
import pl.wrona.webserver.agency.entity.Agency;

public class CalendarEntityMapper {

    public static CalendarEntity apply(CalendarBody calendarBody, Agency agency) {
        return apply(new CalendarEntity(), calendarBody, agency);
    }

    public static CalendarEntity apply(CalendarEntity calendarEntity, CalendarBody calendarBody, Agency agency) {
        calendarEntity.setAgency(agency);

        calendarEntity.setCalendarName(calendarBody.getCalendarName());
        calendarEntity.setDesignation(calendarBody.getDesignation());
        calendarEntity.setDescription(calendarBody.getDescription());

        calendarEntity.setMonday(calendarBody.getMonday());
        calendarEntity.setThursday(calendarBody.getMonday());
        calendarEntity.setMonday(calendarBody.getMonday());
        calendarEntity.setTuesday(calendarBody.getTuesday());
        calendarEntity.setWednesday(calendarBody.getWednesday());
        calendarEntity.setThursday(calendarBody.getThursday());
        calendarEntity.setFriday(calendarBody.getFriday());
        calendarEntity.setSaturday(calendarBody.getSaturday());
        calendarEntity.setSunday(calendarBody.getSunday());

        calendarEntity.setStartDate(calendarBody.getStartDate());
        calendarEntity.setEndDate(calendarBody.getEndDate());
        return calendarEntity;
    }


}
