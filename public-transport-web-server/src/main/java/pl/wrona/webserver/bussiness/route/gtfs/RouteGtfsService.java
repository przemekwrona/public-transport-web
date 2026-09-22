package pl.wrona.webserver.bussiness.route.gtfs;

import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.bussiness.calendar.CalendarDatesQueryService;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.core.calendar.CalendarDatesEntity;
import pl.wrona.webserver.core.calendar.CalendarItemEntity;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Builds a standalone GTFS feed (agency, stops, routes, trips, stop_times, calendar,
 * calendar_dates) scoped to a single route, packaged as a zip archive.
 * <p>
 * Every scheduled departure of the route (a {@link BrigadeEventEntity}) becomes one GTFS trip;
 * its stop-by-stop times are derived the same way as {@code RouteStopTimetableService} computes
 * them for the on-screen timetable: {@code event.startSecond + stopTime.customizedTimeSeconds}.
 */
@Service
@AllArgsConstructor
public class RouteGtfsService {

    private static final DateTimeFormatter GTFS_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final AgencyRepository agencyRepository;
    private final RouteQueryService routeQueryService;
    private final TripQueryService tripQueryService;
    private final BrigadeEventQueryService brigadeEventQueryService;
    private final StopTimeRepository stopTimeRepository;
    private final CalendarDatesQueryService calendarDatesQueryService;

    @PreAgencyAuthorize
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadRouteGtfs(String instance, String routeCode) {
        AgencyEntity agency = agencyRepository.findByAgencyCodeEquals(instance);

        RouteEntity route = routeQueryService.findRouteByAgencyCodeAndRouteCode(instance, routeCode);
        if (route == null) {
            throw new BusinessException("ERROR:202609221200", "Route " + routeCode + " does not exist in agency " + instance);
        }

        List<TripEntity> trips = tripQueryService.findByAgencyCodeAndRoute(instance, route);
        List<Long> tripIds = trips.stream().map(TripEntity::getTripId).toList();

        List<StopTimeEntity> allStopTimes = tripIds.isEmpty() ? List.of() : stopTimeRepository.findAllByTripIdIn(tripIds);
        Map<Long, List<StopTimeEntity>> stopTimesByProfile = allStopTimes.stream()
                .collect(Collectors.groupingBy(stopTime -> stopTime.getTripProfile().getTripProfileId()));
        stopTimesByProfile.values()
                .forEach(list -> list.sort(Comparator.comparingInt(stopTime -> stopTime.getStopTimeId().getStopSequence())));

        List<BrigadeEventEntity> events = brigadeEventQueryService.findAllByTripIds(tripIds);

        Set<StopEntity> usedStops = new LinkedHashSet<>();
        Map<Long, CalendarSymbolEntity> usedCalendars = new LinkedHashMap<>();
        List<GtfsTrip> gtfsTrips = new ArrayList<>();

        for (BrigadeEventEntity event : events) {
            TripProfileEntity tripProfile = event.getTripProfile();
            List<StopTimeEntity> stopTimes = stopTimesByProfile.get(tripProfile.getTripProfileId());
            Optional<CalendarSymbolEntity> calendar = calendarOf(event);
            if (stopTimes == null || stopTimes.isEmpty() || calendar.isEmpty()) {
                continue;
            }
            usedCalendars.putIfAbsent(calendar.get().getServiceId(), calendar.get());
            stopTimes.forEach(stopTime -> usedStops.add(stopTime.getStopEntity()));
            gtfsTrips.add(new GtfsTrip(tripProfile.getTrip(), event, calendar.get(), stopTimes));
        }

        List<CalendarDatesEntity> calendarDates = calendarDatesQueryService.findAllByCalendar(new ArrayList<>(usedCalendars.values()));

        byte[] zip = buildZip(agency, route, usedStops, gtfsTrips, usedCalendars.values(), calendarDates);

        String filename = "gtfs-%s.zip".formatted(routeCode);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(filename))
                .contentLength(zip.length)
                .body(new ByteArrayResource(zip));
    }

    private static Optional<CalendarSymbolEntity> calendarOf(BrigadeEventEntity event) {
        return Optional.ofNullable(event.getResource())
                .map(BrigadeResourceEntity::getBrigadeGroup)
                .map(BrigadeGroupEntity::getCalendarSymbol);
    }

    private byte[] buildZip(AgencyEntity agency, RouteEntity route, Set<StopEntity> stops, List<GtfsTrip> trips,
                             Collection<CalendarSymbolEntity> calendars, List<CalendarDatesEntity> calendarDates) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(buffer, StandardCharsets.UTF_8)) {

            writeEntry(zip, "agency.txt", agencyTxt(agency));
            writeEntry(zip, "stops.txt", stopsTxt(stops));
            writeEntry(zip, "routes.txt", routesTxt(agency, route));
            writeEntry(zip, "trips.txt", tripsTxt(route, trips));
            writeEntry(zip, "stop_times.txt", stopTimesTxt(trips));
            writeEntry(zip, "calendar.txt", calendarTxt(calendars));
            if (!calendarDates.isEmpty()) {
                writeEntry(zip, "calendar_dates.txt", calendarDatesTxt(calendarDates));
            }

            zip.finish();
            return buffer.toByteArray();
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to build GTFS archive for route " + route.getRouteCode(), exception);
        }
    }

    private static void writeEntry(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String agencyTxt(AgencyEntity agency) {
        StringBuilder csv = new StringBuilder("agency_id,agency_name,agency_url,agency_timezone,agency_lang,agency_phone\n");
        csv.append(row(
                agency.getAgencyCode(),
                orEmpty(agency.getAgencyName()),
                orDefault(agency.getAgencyUrl(), "https://" + agency.getAgencyCode()),
                "Europe/Warsaw",
                "pl",
                orEmpty(agency.getAgencyPhone())));
        return csv.toString();
    }

    private static String stopsTxt(Set<StopEntity> stops) {
        StringBuilder csv = new StringBuilder("stop_id,stop_name,stop_lat,stop_lon\n");
        stops.stream()
                .sorted(Comparator.comparing(StopEntity::getStopId))
                .forEach(stop -> csv.append(row(
                        String.valueOf(stop.getStopId()),
                        orEmpty(stop.getName()),
                        String.valueOf(stop.getLat()),
                        String.valueOf(stop.getLon()))));
        return csv.toString();
    }

    private static String routesTxt(AgencyEntity agency, RouteEntity route) {
        StringBuilder csv = new StringBuilder("route_id,agency_id,route_short_name,route_long_name,route_type\n");
        csv.append(row(
                route.getRouteCode(),
                agency.getAgencyCode(),
                orEmpty(route.getLine()),
                orEmpty(route.getName()),
                "3"));
        return csv.toString();
    }

    private static String tripsTxt(RouteEntity route, List<GtfsTrip> trips) {
        Map<String, String> blockIdByTripId = resolveBlockIds(trips);
        StringBuilder csv = new StringBuilder("trip_id,route_id,service_id,trip_headsign,direction_id,block_id\n");
        trips.forEach(trip -> csv.append(row(
                trip.gtfsTripId(),
                route.getRouteCode(),
                String.valueOf(trip.calendar().getServiceId()),
                orEmpty(trip.trip().getHeadsign()),
                trip.trip().getVariantMode() == TripVariantMode.BACK ? "1" : "0",
                blockIdByTripId.getOrDefault(trip.gtfsTripId(), ""))));
        return csv.toString();
    }

    /**
     * A shared block_id tells consumers the same physical vehicle runs both trips back-to-back,
     * so GTFS requires their stop times to never overlap (validator rule
     * "block_trips_with_overlapping_stop_times"). Trips are grouped by resource (the candidate
     * block), and a resource only keeps its block_id if none of its exported trips overlap in
     * time; otherwise every trip in that resource's group falls back to no block_id at all,
     * which always keeps the feed valid.
     */
    private static Map<String, String> resolveBlockIds(List<GtfsTrip> trips) {
        Map<Long, List<GtfsTrip>> byResource = new LinkedHashMap<>();
        for (GtfsTrip trip : trips) {
            Optional.ofNullable(trip.event().getResource())
                    .map(BrigadeResourceEntity::getBrigadeResourceId)
                    .ifPresent(resourceId -> byResource.computeIfAbsent(resourceId, key -> new ArrayList<>()).add(trip));
        }

        Map<String, String> blockIdByTripId = new LinkedHashMap<>();
        byResource.forEach((resourceId, resourceTrips) -> {
            List<GtfsTrip> ordered = new ArrayList<>(resourceTrips);
            ordered.sort(Comparator.comparingInt(RouteGtfsService::tripStartSecond));

            boolean overlaps = false;
            for (int i = 1; i < ordered.size(); i++) {
                if (tripStartSecond(ordered.get(i)) < tripEndSecond(ordered.get(i - 1))) {
                    overlaps = true;
                    break;
                }
            }

            String blockId = overlaps ? "" : "B" + resourceId;
            resourceTrips.forEach(trip -> blockIdByTripId.put(trip.gtfsTripId(), blockId));
        });
        return blockIdByTripId;
    }

    private static int tripStartSecond(GtfsTrip trip) {
        return trip.event().getStartSecond() + trip.stopTimes().stream()
                .mapToInt(StopTimeEntity::getCustomizedTimeSeconds)
                .min()
                .orElse(0);
    }

    private static int tripEndSecond(GtfsTrip trip) {
        return trip.event().getStartSecond() + trip.stopTimes().stream()
                .mapToInt(StopTimeEntity::getCustomizedTimeSeconds)
                .max()
                .orElse(0);
    }

    private static String stopTimesTxt(List<GtfsTrip> trips) {
        StringBuilder csv = new StringBuilder("trip_id,arrival_time,departure_time,stop_id,stop_sequence\n");
        for (GtfsTrip trip : trips) {
            for (StopTimeEntity stopTime : trip.stopTimes()) {
                String time = formatGtfsTime(trip.event().getStartSecond() + stopTime.getCustomizedTimeSeconds());
                csv.append(row(
                        trip.gtfsTripId(),
                        time,
                        time,
                        String.valueOf(stopTime.getStopEntity().getStopId()),
                        String.valueOf(stopTime.getStopTimeId().getStopSequence())));
            }
        }
        return csv.toString();
    }

    private static String calendarTxt(Collection<CalendarSymbolEntity> calendars) {
        StringBuilder csv = new StringBuilder("service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date\n");
        calendars.forEach(calendar -> {
            CalendarItemEntity calendarItem = calendar.getCalendarItem();
            csv.append(row(
                    String.valueOf(calendar.getServiceId()),
                    bit(calendar.isMonday()),
                    bit(calendar.isTuesday()),
                    bit(calendar.isWednesday()),
                    bit(calendar.isThursday()),
                    bit(calendar.isFriday()),
                    bit(calendar.isSaturday()),
                    bit(calendar.isSunday()),
                    formatDate(calendarItem == null ? null : calendarItem.getStartDate()),
                    formatDate(calendarItem == null ? null : calendarItem.getEndDate())));
        });
        return csv.toString();
    }

    private static String calendarDatesTxt(List<CalendarDatesEntity> calendarDates) {
        StringBuilder csv = new StringBuilder("service_id,date,exception_type\n");
        calendarDates.forEach(exception -> csv.append(row(
                String.valueOf(exception.getCalendarDatesId().getServiceId()),
                formatDate(exception.getCalendarDatesId().getDate()),
                String.valueOf(exception.getExceptionType().getGtfsCode()))));
        return csv.toString();
    }

    private static String formatGtfsTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return "%02d:%02d:%02d".formatted(hours, minutes, seconds);
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "" : GTFS_DATE.format(date);
    }

    private static String bit(boolean value) {
        return value ? "1" : "0";
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String orDefault(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static String row(String... values) {
        return Arrays.stream(values)
                .map(RouteGtfsService::csvField)
                .collect(Collectors.joining(",")) + "\n";
    }

    private static String csvField(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private record GtfsTrip(TripEntity trip, BrigadeEventEntity event, CalendarSymbolEntity calendar, List<StopTimeEntity> stopTimes) {

        String gtfsTripId() {
            return "T" + event.getBrigadeEventId();
        }
    }
}
