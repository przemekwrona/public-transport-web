package pl.wrona.webserver.bussiness.route.stop.timetable;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteStopTimetable;
import org.igeolab.iot.pt.server.api.model.RouteStopTimetableDeparture;
import org.igeolab.iot.pt.server.api.model.TripMode;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
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
    private final RouteQueryService routeQueryService;
    private final StopService stopService;
    private final AgencyRepository agencyRepository;
    private final RouteStopTimetablePdfRenderer routeStopTimetablePdfRenderer;

    @PreAgencyAuthorize
    public RouteStopTimetable getRouteStopTimetable(String instance, String routeCode, String stopCode, TripMode tripMode) {
        List<Departure> departures = loadDepartures(instance, routeCode, stopCode, tripMode);
        return new RouteStopTimetable()
                .stopCode(stopCode)
                .tripMode(tripMode)
                .departures(departures.stream().map(Departure::toApi).toList());
    }

    @PreAgencyAuthorize
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getRouteStopTimetablePdf(String instance, String routeCode, String stopCode, TripMode tripMode) {
        long stopId = parseStopCode(stopCode);
        AgencyEntity agency = agencyRepository.findByAgencyCodeEquals(instance);
        RouteEntity route = routeQueryService.findRouteByAgencyCodeAndRouteCode(instance, routeCode);
        StopEntity stop = stopService.mapStopByIdsIn(List.of(stopId)).get(stopId);
        TripVariantMode variantMode = TripVariantModeMapper.map(tripMode);
        List<TripEntity> trips = tripQueryService.findByAgencyAndRouteCodeAndVariantMode(instance, routeCode, variantMode);
        List<Departure> departures = departuresAtStop(trips, stopId);
        RouteStopTimetableView timetable = new RouteStopTimetableView(
                agencyName(agency),
                line(route),
                routeName(route),
                stopName(stop, stopCode),
                direction(trips, route, tripMode),
                tripMode == null ? "" : tripMode.getValue(),
                calendars(departures),
                legend(trips, departures));
        byte[] pdf = routeStopTimetablePdfRenderer.render(timetable);
        String filename = "timetable-%s-%s-%s.pdf".formatted(routeCode, stopCode, tripMode);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"%s\"".formatted(filename))
                .contentLength(pdf.length)
                .body(new ByteArrayResource(pdf));
    }

    private List<Departure> loadDepartures(String instance, String routeCode, String stopCode, TripMode tripMode) {
        long stopId = parseStopCode(stopCode);
        TripVariantMode variantMode = TripVariantModeMapper.map(tripMode);
        List<TripEntity> trips = tripQueryService.findByAgencyAndRouteCodeAndVariantMode(instance, routeCode, variantMode);
        return departuresAtStop(trips, stopId);
    }

    private List<Departure> departuresAtStop(List<TripEntity> trips, long stopId) {
        List<Long> tripIds = trips.stream().map(TripEntity::getTripId).toList();
        if (tripIds.isEmpty()) {
            return List.of();
        }

        Map<Long, StopTimeEntity> stopTimeByProfileId = stopTimeRepository.findAllByTripIdInAndStopId(tripIds, stopId).stream()
                .collect(Collectors.toMap(
                        stopTime -> stopTime.getTripProfile().getTripProfileId(),
                        Function.identity(),
                        RouteStopTimetableService::earlierInSequence));

        if (stopTimeByProfileId.isEmpty()) {
            return List.of();
        }

        return brigadeEventQueryService.findAllByTripIds(tripIds).stream()
                .map(event -> toDeparture(event, stopTimeByProfileId.get(event.getTripProfile().getTripProfileId())))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Departure::hour).thenComparing(Departure::minute))
                .toList();
    }

    private static List<RouteStopTimetableView.CalendarTimetable> calendars(List<Departure> departures) {
        Map<CalendarKey, List<Departure>> byCalendar = departures.stream()
                .collect(Collectors.groupingBy(Departure::calendar, LinkedHashMap::new, Collectors.toList()));

        return byCalendar.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new RouteStopTimetableView.CalendarTimetable(
                        entry.getKey().designation(),
                        entry.getKey().description(),
                        entry.getKey().title(),
                        hours(entry.getValue())))
                .filter(calendar -> !calendar.hours().isEmpty())
                .toList();
    }

    private static List<RouteStopTimetableView.HourRow> hours(List<Departure> departures) {
        return departures.stream()
                .collect(Collectors.groupingBy(Departure::hour, LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new RouteStopTimetableView.HourRow(
                        "%02d".formatted(entry.getKey()),
                        entry.getValue().stream()
                                .map(departure -> new RouteStopTimetableView.MinuteCell(
                                        "%02d".formatted(departure.minute()),
                                        departure.symbol()))
                                .toList()))
                .toList();
    }

    private static List<RouteStopTimetableView.LegendEntry> legend(List<TripEntity> trips, List<Departure> departures) {
        var usedSymbols = departures.stream()
                .map(Departure::symbol)
                .filter(RouteStopTimetableService::hasText)
                .collect(Collectors.toSet());

        Map<String, String> descriptionBySymbol = new LinkedHashMap<>();
        trips.stream()
                .filter(trip -> hasText(trip.getVariantDesignation()) && usedSymbols.contains(trip.getVariantDesignation()))
                .forEach(trip -> descriptionBySymbol.putIfAbsent(
                        trip.getVariantDesignation(),
                        Optional.ofNullable(trip.getVariantDescription()).orElse("")));

        return descriptionBySymbol.entrySet().stream()
                .map(entry -> new RouteStopTimetableView.LegendEntry(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static Departure toDeparture(BrigadeEventEntity event, StopTimeEntity stopTime) {
        if (event == null || stopTime == null) {
            return null;
        }
        LocalTime time = LocalTime.MIN.plusSeconds(event.getStartSecond() + stopTime.getCustomizedTimeSeconds());
        TripEntity trip = event.getTripProfile().getTrip();
        return new Departure(
                calendarKey(event),
                time.getHour(),
                time.getMinute(),
                time.format(TIME_FORMATTER),
                Optional.ofNullable(trip.getVariantDesignation()).orElse(""),
                trip.getTripCode());
    }

    private static CalendarKey calendarKey(BrigadeEventEntity event) {
        return Optional.ofNullable(event.getResource())
                .map(BrigadeResourceEntity::getBrigadeGroup)
                .map(BrigadeGroupEntity::getCalendarSymbol)
                .map(calendar -> new CalendarKey(
                        Optional.ofNullable(calendar.getDesignation()).orElse(""),
                        Optional.ofNullable(calendar.getDescription()).orElse("")))
                .orElseGet(CalendarKey::unsorted);
    }

    private static StopTimeEntity earlierInSequence(StopTimeEntity left, StopTimeEntity right) {
        return left.getStopTimeId().getStopSequence() <= right.getStopTimeId().getStopSequence() ? left : right;
    }

    private static String agencyName(AgencyEntity agency) {
        return agency == null ? "" : Optional.ofNullable(agency.getAgencyName()).orElse("");
    }

    private static String line(RouteEntity route) {
        return route == null ? "" : Optional.ofNullable(route.getLine()).orElse("");
    }

    private static String routeName(RouteEntity route) {
        return route == null ? "" : Optional.ofNullable(route.getName()).orElse("");
    }

    private static String stopName(StopEntity stop, String stopCode) {
        return Optional.ofNullable(stop)
                .map(StopEntity::getName)
                .filter(RouteStopTimetableService::hasText)
                .orElse(stopCode);
    }

    private static String direction(List<TripEntity> trips, RouteEntity route, TripMode tripMode) {
        return trips.stream()
                .filter(TripEntity::isMainVariant)
                .map(TripEntity::getHeadsign)
                .filter(RouteStopTimetableService::hasText)
                .findFirst()
                .or(() -> Optional.ofNullable(route).map(entity -> TripMode.BACK.equals(tripMode)
                        ? entity.getOriginStopName()
                        : entity.getDestinationStopName()))
                .filter(RouteStopTimetableService::hasText)
                .orElse("");
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static long parseStopCode(String stopCode) {
        try {
            return Long.parseLong(stopCode);
        } catch (NumberFormatException exception) {
            throw new BusinessException("ERROR:202609072200", "Stop code " + stopCode + " is not valid");
        }
    }

    static boolean wantsPdf(String accept) {
        if (accept == null || accept.isBlank()) {
            return false;
        }
        List<MediaType> accepted = MediaType.parseMediaTypes(accept);
        MediaType.sortBySpecificityAndQuality(accepted);
        for (MediaType mediaType : accepted) {
            if (MediaType.ALL.equalsTypeAndSubtype(mediaType)) {
                return false;
            }
            if (mediaType.isCompatibleWith(MediaType.APPLICATION_PDF)) {
                return true;
            }
            if (mediaType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
                return false;
            }
        }
        return false;
    }

    private record Departure(CalendarKey calendar, int hour, int minute, String time, String symbol, String tripCode) {

        RouteStopTimetableDeparture toApi() {
            return new RouteStopTimetableDeparture()
                    .h(hour)
                    .m(minute)
                    .time(time)
                    .symbol(symbol)
                    .tripCode(tripCode);
        }
    }

    private record CalendarKey(String designation, String description) implements Comparable<CalendarKey> {

        static CalendarKey unsorted() {
            return new CalendarKey("", "Rozkład");
        }

        String title() {
            if (hasText(designation) && hasText(description)) {
                return designation + " — " + description;
            }
            if (hasText(description)) {
                return description;
            }
            if (hasText(designation)) {
                return designation;
            }
            return "Rozkład";
        }

        @Override
        public int compareTo(CalendarKey other) {
            int designationOrder = designation.compareToIgnoreCase(other.designation);
            if (designationOrder != 0) {
                return designationOrder;
            }
            return description.compareToIgnoreCase(other.description);
        }
    }
}
