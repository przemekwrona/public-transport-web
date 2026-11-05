package pl.wrona.webserver.bussiness.stop.timetable;

import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.bussiness.stop.name.AgencyStopNameService;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.TerritorialUnitQueryService;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.core.entity.TerritorialUnitEntity;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.text.Collator;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AgencyStopTimetableService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final StopTimeRepository stopTimeRepository;
    private final BrigadeEventQueryService brigadeEventQueryService;
    private final StopService stopService;
    private final AgencyRepository agencyRepository;
    private final TerritorialUnitQueryService territorialUnitQueryService;
    private final AgencyStopNameService agencyStopNameService;
    private final AgencyStopTimetablePdfRenderer agencyStopTimetablePdfRenderer;

    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER,
            PreAgencyAuthorize.Role.BRIGADIER,
            PreAgencyAuthorize.Role.DISPATCHER,
            PreAgencyAuthorize.Role.DRIVER
    })
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadAgencyStopTimetable(String instance, Long stopId) {
        AgencyStopTimetableView timetable = timetable(instance, stopId);
        byte[] pdf = agencyStopTimetablePdfRenderer.render(timetable);
        String filename = "rozklad-przystanek-%s.pdf".formatted(stopId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(filename))
                .contentLength(pdf.length)
                .body(new ByteArrayResource(pdf));
    }

    private AgencyStopTimetableView timetable(String agency, Long stopId) {
        StopEntity stop = stopService.mapStopByIdsIn(List.of(stopId)).get(stopId);
        if (stop == null) {
            throw new BusinessException("ERROR:202611051139", "Stop " + stopId + " does not exist");
        }
        AgencyEntity agencyEntity = agencyRepository.findByAgencyCodeEquals(agency);
        List<StopTimeEntity> visits = stopTimeRepository.findAllByAgencyCodeAndStopId(agency, stopId);
        Board board = board(agency, stopId, visits);
        return new AgencyStopTimetableView(
                agencyName(agencyEntity),
                stopName(agency, stop),
                board.directions(),
                board.legend());
    }

    private Board board(String agency, Long stopId, List<StopTimeEntity> visits) {
        List<Long> tripIds = visits.stream()
                .map(AgencyStopTimetableService::tripId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (tripIds.isEmpty()) {
            return new Board(List.of(), List.of());
        }

        List<StopTimeEntity> tripStops = stopTimeRepository.findAllByTripIdIn(tripIds);
        Map<Long, List<StopTimeEntity>> visitsByProfile = visits.stream()
                .filter(stopTime -> profileId(stopTime) != null)
                .collect(Collectors.groupingBy(AgencyStopTimetableService::profileId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, StopTimeEntity> lastByProfile = lastStopByProfile(tripStops);
        Map<Long, String> localities = localities(lastByProfile);
        Map<Long, String> names = agencyStopNameService.findOverrides(agency, stopIds(stopId, lastByProfile));

        Collator order = polishCollator();
        Collator equality = polishCollator();
        equality.setStrength(Collator.SECONDARY);

        Map<String, List<ScheduledDeparture>> byDirection = new LinkedHashMap<>();
        Set<AgencyStopTimetableView.LegendEntry> legend = new LinkedHashSet<>();
        for (BrigadeEventEntity event : brigadeEventQueryService.findAllByTripIds(tripIds)) {
            Long profileId = event.getTripProfile() == null ? null : event.getTripProfile().getTripProfileId();
            if (profileId == null || event.getStartSecond() == null) {
                continue;
            }
            StopTimeEntity destination = lastByProfile.get(profileId);
            List<StopTimeEntity> profileVisits = visitsByProfile.getOrDefault(profileId, List.of());
            for (StopTimeEntity visit : profileVisits) {
                if (isTerminus(visit, destination)) {
                    continue;
                }
                ScheduledDeparture departure = departure(event, visit, destination, localities, names, equality, legend);
                byDirection.computeIfAbsent(departure.direction(), ignored -> new ArrayList<>()).add(departure);
            }
        }

        List<AgencyStopTimetableView.DirectionRow> rows = byDirection.entrySet().stream()
                .map(entry -> new AgencyStopTimetableView.DirectionRow(entry.getKey(), times(entry.getValue(), order)))
                .filter(row -> !row.departures().isEmpty())
                .sorted(Comparator.comparing(AgencyStopTimetableView.DirectionRow::direction, order))
                .toList();
        List<AgencyStopTimetableView.LegendEntry> legendRows = legend.stream()
                .sorted(Comparator
                        .comparing(AgencyStopTimetableView.LegendEntry::symbol, order)
                        .thenComparing(AgencyStopTimetableView.LegendEntry::description, order))
                .toList();
        return new Board(rows, legendRows);
    }

    private static boolean isTerminus(StopTimeEntity visit, StopTimeEntity destination) {
        if (destination == null || visit.getStopTimeId() == null || destination.getStopTimeId() == null) {
            return false;
        }
        return visit.getStopTimeId().getStopSequence() >= destination.getStopTimeId().getStopSequence();
    }

    private static ScheduledDeparture departure(
            BrigadeEventEntity event,
            StopTimeEntity visit,
            StopTimeEntity destination,
            Map<Long, String> localities,
            Map<Long, String> names,
            Collator equality,
            Set<AgencyStopTimetableView.LegendEntry> legend) {
        int seconds = event.getStartSecond() + visit.getCustomizedTimeSeconds();
        LocalTime time = LocalTime.MIN.plusSeconds(seconds);
        TripEntity trip = event.getTripProfile().getTrip();
        CalendarSymbolEntity calendar = calendar(event);
        String calendarSymbol = calendar == null ? "" : text(calendar.getDesignation());
        String tripSymbol = trip == null ? "" : text(trip.getVariantDesignation());
        remember(legend, calendarSymbol, calendar == null ? "" : calendar.getDescription());
        remember(legend, tripSymbol, trip == null ? "" : trip.getVariantDescription());
        return new ScheduledDeparture(
                direction(trip, destination, localities, names, equality),
                seconds,
                time.format(TIME_FORMATTER),
                symbols(calendarSymbol, tripSymbol, equality));
    }

    private static List<AgencyStopTimetableView.DepartureTime> times(List<ScheduledDeparture> departures, Collator order) {
        return departures.stream()
                .sorted(Comparator
                        .comparingInt(ScheduledDeparture::seconds)
                        .thenComparing(departure -> String.join("", departure.symbols()), order))
                .collect(Collectors.toMap(
                        departure -> departure.time() + "|" + String.join("", departure.symbols()),
                        departure -> new AgencyStopTimetableView.DepartureTime(departure.time(), departure.symbols()),
                        (left, right) -> left,
                        LinkedHashMap::new))
                .values().stream()
                .toList();
    }

    private Map<Long, String> localities(Map<Long, StopTimeEntity> lastByProfile) {
        List<Long> territoryIds = lastByProfile.values().stream()
                .map(StopTimeEntity::getStopEntity)
                .filter(Objects::nonNull)
                .map(StopEntity::getTerritorialUnitId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (territoryIds.isEmpty()) {
            return Map.of();
        }
        return territorialUnitQueryService.findAllByTerritoriesIdIn(territoryIds).stream()
                .filter(unit -> unit.getTerritorialUnitId() != null)
                .collect(Collectors.toMap(
                        TerritorialUnitEntity::getTerritorialUnitId,
                        unit -> text(unit.getNazwa()),
                        (left, right) -> left));
    }

    private static Map<Long, StopTimeEntity> lastStopByProfile(List<StopTimeEntity> tripStops) {
        Map<Long, StopTimeEntity> last = new LinkedHashMap<>();
        for (StopTimeEntity stopTime : tripStops) {
            Long profileId = profileId(stopTime);
            if (profileId == null) {
                continue;
            }
            last.merge(profileId, stopTime, AgencyStopTimetableService::laterInSequence);
        }
        return last;
    }

    private static StopTimeEntity laterInSequence(StopTimeEntity left, StopTimeEntity right) {
        return left.getStopTimeId().getStopSequence() >= right.getStopTimeId().getStopSequence() ? left : right;
    }

    private String stopName(String agency, StopEntity stop) {
        String preferred = AgencyStopNameService.prefer(
                stop.getName(),
                agencyStopNameService.findOverrides(agency, List.of(stop.getStopId())),
                stop.getStopId());
        return hasText(preferred) ? preferred : "Przystanek";
    }

    private static String direction(
            TripEntity trip,
            StopTimeEntity destination,
            Map<Long, String> localities,
            Map<Long, String> names,
            Collator equality) {
        StopEntity destinationStop = destination == null ? null : destination.getStopEntity();
        String headsign = trip == null ? "" : text(trip.getHeadsign());
        String stopName = destinationStop == null
                ? ""
                : text(AgencyStopNameService.prefer(destinationStop.getName(), names, destinationStop.getStopId()));
        String directionName = hasText(headsign) ? headsign : stopName;
        String locality = destinationStop == null || destinationStop.getTerritorialUnitId() == null
                ? ""
                : text(localities.get(destinationStop.getTerritorialUnitId()));
        if (!hasText(directionName)) {
            return hasText(locality) ? locality : "—";
        }
        if (!hasText(locality) || equality.compare(directionName, locality) == 0) {
            return directionName;
        }
        return directionName + " / " + locality;
    }

    private static List<String> symbols(String calendarSymbol, String tripSymbol, Collator equality) {
        List<String> symbols = new ArrayList<>();
        if (hasText(calendarSymbol)) {
            symbols.add(calendarSymbol);
        }
        if (hasText(tripSymbol) && symbols.stream().noneMatch(present -> equality.compare(present, tripSymbol) == 0)) {
            symbols.add(tripSymbol);
        }
        symbols.sort(equality);
        return List.copyOf(symbols);
    }

    private static void remember(Set<AgencyStopTimetableView.LegendEntry> legend, String symbol, String description) {
        if (!hasText(symbol)) {
            return;
        }
        legend.add(new AgencyStopTimetableView.LegendEntry(symbol, text(description)));
    }

    private static List<Long> stopIds(Long stopId, Map<Long, StopTimeEntity> lastByProfile) {
        Set<Long> ids = new LinkedHashSet<>();
        ids.add(stopId);
        lastByProfile.values().stream()
                .map(StopTimeEntity::getStopEntity)
                .filter(Objects::nonNull)
                .map(StopEntity::getStopId)
                .filter(Objects::nonNull)
                .forEach(ids::add);
        return List.copyOf(ids);
    }

    private static CalendarSymbolEntity calendar(BrigadeEventEntity event) {
        return Optional.ofNullable(event.getResource())
                .map(BrigadeResourceEntity::getBrigadeGroup)
                .map(BrigadeGroupEntity::getCalendarSymbol)
                .orElse(null);
    }

    private static Long tripId(StopTimeEntity stopTime) {
        if (stopTime.getTripProfile() == null || stopTime.getTripProfile().getTrip() == null) {
            return null;
        }
        return stopTime.getTripProfile().getTrip().getTripId();
    }

    private static Long profileId(StopTimeEntity stopTime) {
        return stopTime.getTripProfile() == null ? null : stopTime.getTripProfile().getTripProfileId();
    }

    private static String agencyName(AgencyEntity agency) {
        return agency == null ? "" : text(agency.getAgencyName());
    }

    private static Collator polishCollator() {
        return Collator.getInstance(Locale.forLanguageTag("pl"));
    }

    private static String text(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record ScheduledDeparture(String direction, int seconds, String time, List<String> symbols) {
    }

    private record Board(
            List<AgencyStopTimetableView.DirectionRow> directions,
            List<AgencyStopTimetableView.LegendEntry> legend
    ) {
    }
}
