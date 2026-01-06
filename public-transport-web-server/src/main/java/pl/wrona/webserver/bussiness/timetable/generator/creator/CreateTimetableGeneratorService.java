package pl.wrona.webserver.bussiness.timetable.generator.creator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CreateTimetableGeneratorRequest;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorPayload;
import org.igeolab.iot.pt.server.api.model.TimetableStopTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.bussiness.timetable.generator.TimetableGeneratorCommandService;
import pl.wrona.webserver.bussiness.timetable.generator.TimetableGeneratorItemCommandService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.calendar.CalendarEntity;
import pl.wrona.webserver.core.calendar.CalendarQueryService;
import pl.wrona.webserver.core.timetable.TimetableDeparture;
import pl.wrona.webserver.core.timetable.TimetableGeneratorEntity;
import pl.wrona.webserver.core.timetable.TimetableGeneratorItemEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.time.LocalTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CreateTimetableGeneratorService {

    private final CalendarQueryService calendarQueryService;
    private final RouteQueryService routeQueryService;
    private final TripQueryService tripQueryService;
    private final TimetableGeneratorCommandService timetableGeneratorCommandService;
    private final TimetableGeneratorItemCommandService timetableGeneratorItemCommandService;

    @Transactional
    @PreAgencyAuthorize
    public CreateTimetableGeneratorRequest createTimetableGenerator(String instance, CreateTimetableGeneratorRequest request) {
        CalendarEntity calendarEntity = calendarQueryService.getCalendar(instance, request.getTimetables().getCalendarName());
        RouteEntity routeEntity = routeQueryService.findRouteByAgencyCodeAndRouteId(instance, new RouteId()
                .line(request.getRouteId().getLine())
                .name(request.getRouteId().getName()));

        TimetableGeneratorEntity timetable = TimetableGeneratorEntity.builder()
                .build();

        TimetableGeneratorEntity savedTimetable = timetableGeneratorCommandService.save(timetable);

        TimetableGeneratorItemEntity item = new TimetableGeneratorItemEntity();
        Optional.of(request)
                .map(CreateTimetableGeneratorRequest::getTimetables)
                .map(TimetableGeneratorPayload::getFront)
                .ifPresent(payload -> {
                    item.setFrontStartTime(LocalTime.parse(payload.getStartDate()));
                    item.setFrontEndTime(LocalTime.parse(payload.getEndDate()));
                    item.setFrontInterval(payload.getInterval());

                    item.setFrontPayload(payload.getDepartures().stream()
                            .map(CreateTimetableGeneratorService::buildDeparture)
                            .toList());
                });

        Optional.of(request)
                .map(CreateTimetableGeneratorRequest::getTimetables)
                .map(TimetableGeneratorPayload::getBack)
                .ifPresent(payload -> {
                    item.setBackStartTime(LocalTime.parse(payload.getStartDate()));
                    item.setBackEndTime(LocalTime.parse(payload.getEndDate()));
                    item.setBackInterval(payload.getInterval());

                    item.setBackPayload(payload.getDepartures().stream()
                            .map(CreateTimetableGeneratorService::buildDeparture)
                            .toList());
                });

        item.setCalendar(calendarEntity);
        item.setRouteLine(request.getRouteId().getLine());
        item.setRouteName(request.getRouteId().getName());
        item.setRouteVersion(1);
        item.setRoute(routeEntity);

        item.setTimetableGenerator(savedTimetable);

        TimetableGeneratorItemEntity savedTimetableItem = timetableGeneratorItemCommandService.save(item);

        return null;
    }

    private static TimetableDeparture buildDeparture(TimetableStopTime departure) {
        return TimetableDeparture.builder()
                .time(LocalTime.parse(departure.getTime()))
                .symbol(departure.getDesignation())
                .build();
    }

}
