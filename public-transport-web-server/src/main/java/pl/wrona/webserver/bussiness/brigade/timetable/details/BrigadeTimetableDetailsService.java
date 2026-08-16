package pl.wrona.webserver.bussiness.brigade.timetable.details;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.GetTimetableByBrigadeResponse;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BrigadeTimetableDetailsService {

    private final BrigadeEventQueryService brigadeEventQueryService;

    @PreAgencyAuthorize
    public GetTimetableByBrigadeResponse getTimetableByBrigadeAndCalendarSymbol(
            String instance, String brigadeCode, String calendarCode, String calendarSymbol) {
        List<BrigadeEventEntity> events = brigadeEventQueryService.findAllWithTripByAgencyAndBrigadeAndCalendarAndSymbol(
                instance, brigadeCode, calendarCode, calendarSymbol);

        Map<RouteId, List<BrigadeEventEntity>> eventsByRouteId = events.stream()
                .collect(Collectors.groupingBy(
                        BrigadeTimetableDetailsService::toRouteId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        return new GetTimetableByBrigadeResponse();
    }

    private static RouteId toRouteId(BrigadeEventEntity event) {
        return new RouteId()
                .line(event.getTrip().getRoute().getLine())
                .name(event.getTrip().getRoute().getName());
    }
}
