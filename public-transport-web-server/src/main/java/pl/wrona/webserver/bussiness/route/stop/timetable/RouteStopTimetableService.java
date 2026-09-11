package pl.wrona.webserver.bussiness.route.stop.timetable;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteStopTimetable;
import org.igeolab.iot.pt.server.api.model.RouteStopTimetableDeparture;
import org.igeolab.iot.pt.server.api.model.TripMode;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RouteStopTimetableService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final TripQueryService tripQueryService;
    private final BrigadeEventQueryService brigadeEventQueryService;
    private final StopTimeRepository stopTimeRepository;

    @PreAgencyAuthorize
    public RouteStopTimetable getRouteStopTimetable(String instance, String routeCode, String stopCode, TripMode tripMode) {
        long stopId = parseStopCode(stopCode);
        TripVariantMode variantMode = TripVariantModeMapper.map(tripMode);
        List<TripEntity> trips = tripQueryService.findByAgencyAndRouteCodeAndVariantMode(instance, routeCode, variantMode);
        List<Long> tripIds = trips.stream().map(TripEntity::getTripId).toList();

        if (tripIds.isEmpty()) {
            return emptyTimetable(stopCode, tripMode);
        }

        Map<Long, StopTimeEntity> stopTimeByProfileId = stopTimeRepository.findAllByTripIdInAndStopId(tripIds, stopId).stream()
                .collect(Collectors.toMap(
                        stopTime -> stopTime.getTripProfile().getTripProfileId(),
                        Function.identity(),
                        RouteStopTimetableService::earlierInSequence));

        if (stopTimeByProfileId.isEmpty()) {
            return emptyTimetable(stopCode, tripMode);
        }

        List<RouteStopTimetableDeparture> departures = brigadeEventQueryService.findAllByTripIds(tripIds).stream()
                .map(event -> toDeparture(event, stopTimeByProfileId.get(event.getTripProfile().getTripProfileId())))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(RouteStopTimetableDeparture::getH)
                        .thenComparing(RouteStopTimetableDeparture::getM))
                .toList();

        return new RouteStopTimetable()
                .stopCode(stopCode)
                .tripMode(tripMode)
                .departures(departures);
    }

    private static RouteStopTimetable emptyTimetable(String stopCode, TripMode tripMode) {
        return new RouteStopTimetable()
                .stopCode(stopCode)
                .tripMode(tripMode)
                .departures(List.of());
    }

    private static RouteStopTimetableDeparture toDeparture(BrigadeEventEntity event, StopTimeEntity stopTime) {
        if (event == null || stopTime == null) {
            return null;
        }
        LocalTime time = LocalTime.MIN.plusSeconds(event.getStartSecond() + stopTime.getCustomizedTimeSeconds());
        TripEntity trip = event.getTripProfile().getTrip();
        return new RouteStopTimetableDeparture()
                .h(time.getHour())
                .m(time.getMinute())
                .time(time.format(TIME_FORMATTER))
                .symbol(Optional.ofNullable(trip.getVariantDesignation()).orElse(""))
                .tripCode(trip.getTripCode());
    }

    private static StopTimeEntity earlierInSequence(StopTimeEntity left, StopTimeEntity right) {
        return left.getStopTimeId().getStopSequence() <= right.getStopTimeId().getStopSequence() ? left : right;
    }

    private static long parseStopCode(String stopCode) {
        try {
            return Long.parseLong(stopCode);
        } catch (NumberFormatException exception) {
            throw new BusinessException("ERROR:202609072200", "Stop code " + stopCode + " is not valid");
        }
    }
}
