package pl.wrona.webserver.bussiness.timetable.generator.creator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CreateTimetableGeneratorRequest;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.calendar.CalendarEntity;
import pl.wrona.webserver.core.calendar.CalendarQueryService;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class CreateTimetableGeneratorService {

    private final CalendarQueryService calendarQueryService;
    private final RouteQueryService routeQueryService;
    private final TripQueryService tripQueryService;

    @PreAgencyAuthorize
    public CreateTimetableGeneratorRequest createTimetableGenerator(String instance, CreateTimetableGeneratorRequest createTimetableGeneratorRequest) {
        CalendarEntity calendarEntity = calendarQueryService.getCalendar(instance, createTimetableGeneratorRequest.getTimetables().getCalendarName());
        RouteEntity routeEntity = routeQueryService.findRouteByAgencyCodeAndRouteId(instance, new RouteId()
                .line(createTimetableGeneratorRequest.getRouteId().getLine())
                .name(createTimetableGeneratorRequest.getRouteId().getName()));

        return null;
    }

}
