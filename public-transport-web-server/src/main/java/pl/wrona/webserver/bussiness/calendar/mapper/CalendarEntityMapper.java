package pl.wrona.webserver.bussiness.calendar.mapper;

import org.igeolab.iot.pt.server.api.model.CalendarBody;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;

public class CalendarEntityMapper {

    public static CalendarSymbolEntity apply(CalendarBody calendarBody, AgencyEntity agencyEntity) {
        return apply(new CalendarSymbolEntity(), calendarBody, agencyEntity);
    }

    public static CalendarSymbolEntity apply(CalendarSymbolEntity calendarSymbolEntity, CalendarBody calendarBody, AgencyEntity agencyEntity) {
        calendarSymbolEntity.setAgency(agencyEntity);

        calendarSymbolEntity.setCalendarName(calendarBody.getCalendarName());
        calendarSymbolEntity.setDesignation(calendarBody.getDesignation());
        calendarSymbolEntity.setDescription(calendarBody.getDescription());

        calendarSymbolEntity.setMonday(calendarBody.getMonday());
        calendarSymbolEntity.setThursday(calendarBody.getMonday());
        calendarSymbolEntity.setMonday(calendarBody.getMonday());
        calendarSymbolEntity.setTuesday(calendarBody.getTuesday());
        calendarSymbolEntity.setWednesday(calendarBody.getWednesday());
        calendarSymbolEntity.setThursday(calendarBody.getThursday());
        calendarSymbolEntity.setFriday(calendarBody.getFriday());
        calendarSymbolEntity.setSaturday(calendarBody.getSaturday());
        calendarSymbolEntity.setSunday(calendarBody.getSunday());

        calendarSymbolEntity.setStartDate(calendarBody.getStartDate());
        calendarSymbolEntity.setEndDate(calendarBody.getEndDate());
        return calendarSymbolEntity;
    }


}
