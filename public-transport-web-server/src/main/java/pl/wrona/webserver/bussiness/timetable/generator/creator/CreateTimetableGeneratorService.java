package pl.wrona.webserver.bussiness.timetable.generator.creator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CreateTimetableGeneratorRequest;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.calendar.CalendarEntity;
import pl.wrona.webserver.core.calendar.CalendarQueryService;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class CreateTimetableGeneratorService {

    private CalendarQueryService calendarQueryService;

    @PreAgencyAuthorize
    public CreateTimetableGeneratorRequest createTimetableGenerator(String instance, CreateTimetableGeneratorRequest createTimetableGeneratorRequest) {
        CalendarEntity calendarEntity = calendarQueryService.getCalendar(instance, createTimetableGeneratorRequest.getTimetables().getCalendarId());
        return null;
    }

}
