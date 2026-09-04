package pl.wrona.webserver.bussiness.brigade.timetable.details;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeTimetableDeparture;
import org.igeolab.iot.pt.server.api.model.BrigadeTimetableTrip;
import org.igeolab.iot.pt.server.api.model.BrigadeTimetableVariant;
import org.igeolab.iot.pt.server.api.model.GetTimetableByBrigadeResponse;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.RouteId1;
import org.igeolab.iot.pt.server.api.model.TripId2;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceQueryService;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BrigadeTimetableDetailsService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final BrigadeResourceQueryService brigadeResourceQueryService;
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

        List<BrigadeTimetableTrip> trips = eventsByRouteId.entrySet().stream()
                .map(entry -> toBrigadeTimetableTrip(entry.getKey(), entry.getValue()))
                .toList();

        return new GetTimetableByBrigadeResponse().trips(trips);
    }

    private static BrigadeTimetableTrip toBrigadeTimetableTrip(RouteId routeId, List<BrigadeEventEntity> events) {
        return new BrigadeTimetableTrip()
                .tripId(new TripId2().routeId(new RouteId1()
                        .line(routeId.getLine())
                        .name(routeId.getName())
                        .version(routeId.getVersion())))
                .front(toVariant(events, TripVariantMode.FRONT))
                .back(toVariant(events, TripVariantMode.BACK));
    }

    private static BrigadeTimetableVariant toVariant(List<BrigadeEventEntity> events, TripVariantMode variantMode) {
        List<BrigadeTimetableDeparture> departures = events.stream()
                .filter(event -> variantMode.equals(event.getTripProfile().getTrip().getVariantMode()))
                .map(BrigadeTimetableDetailsService::toDeparture)
                .toList();

        return new BrigadeTimetableVariant().departures(departures);
    }

    private static BrigadeTimetableDeparture toDeparture(BrigadeEventEntity event) {
        var time = LocalTime.MIN.plusSeconds(event.getStartSecond());
        return new BrigadeTimetableDeparture()
                .h(time.getHour())
                .m(time.getMinute())
                .time(time.format(TIME_FORMATTER))
                .symbol("");
    }

    private static RouteId toRouteId(BrigadeEventEntity event) {
        return new RouteId()
                .line(event.getTripProfile().getTrip().getRoute().getLine())
                .name(event.getTripProfile().getTrip().getRoute().getName());
    }
}
