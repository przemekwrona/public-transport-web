package pl.wrona.webserver.bussiness.gtfs.download;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.brigade.BrigadeTripEntity;
import pl.wrona.webserver.core.calendar.CalendarDatesService;
import pl.wrona.webserver.core.agency.AgencyEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class GtfsDownloadService {

    private final AgencyService agencyService;
    private final StopService stopService;

    private final CalendarDatesService calendarDatesService;

    private final GtfsCalendarService gtfsCalendarService;
    private final GtfsRouteService gtfsRouteService;
    private final GtfsBrigadeTripService gtfsBrigadeTripService;
    private final GtfsStopTimeService gtfsStopTimeService;

    public Resource downloadGtfs(String agency) {
        try {
            Path tempDir = Files.createTempDirectory("gtfs_");
            File tempFile = tempDir.toFile();

            GtfsWriter writer = new GtfsWriter();
            writer.setOutputLocation(tempFile);

            LocalDate now = LocalDate.now();
            LocalDate nowPlus30Days = LocalDate.now().plusDays(30);

            // handle FeedInfo
            writer.handleEntity(getFeedInfo(now, nowPlus30Days));

            // hande Agency
            AgencyEntity agencyEntity = agencyService.findAgencyByAgencyCode(agency);
            writer.handleEntity(AgencyHandler.handle(agencyEntity));

            // handle Calendar
            var activeCalendars = gtfsCalendarService.getActiveCalendars(agency);
            var calendars = activeCalendars
                    .stream().map(CalendarHandler::handle)
                    .toList();
            calendars.forEach(writer::handleEntity);

            var calendarDictionary = calendars.stream()
                    .collect(Collectors.toMap(calendar -> calendar.getServiceId().getId(), Function.identity()));

            // handle Calendar Date
            calendarDatesService.findAllActiveCalendarDate().stream()
                    .map(CalendarDateHandler::handle)
                    .forEach(writer::handleEntity);

            // handle Stops
            var stops = stopService.findAllStops(agencyEntity).stream()
                    .map(stop -> StopHandler.handle(agencyEntity, stop))
                    .toList();

            stops.forEach(writer::handleEntity);

            var stopDictionary = stops.stream().collect(Collectors.toMap(stop -> Long.parseLong(stop.getId().getId()), Function.identity()));

            // handle Routes
            var routes = gtfsRouteService.findAllRoutes(agencyEntity).stream()
                    .map(route -> RouteHandler.handle(agencyEntity, route))
                    .toList();

            routes.forEach(writer::handleEntity);

            var routesDictionary = routes.stream()
                    .collect(Collectors.toMap(route -> route.getId().getId(), Function.identity()));

            // handle Trips
            var brigadeTripEntities = gtfsBrigadeTripService.findAllByAgencyAndActiveCalendars(agencyEntity, activeCalendars);
            var brigadeTrips = brigadeTripEntities.stream()
                    .map(brigadeTrip -> {
                        AgencyAndId agencyAndId = new AgencyAndId();
                        agencyAndId.setAgencyId(agencyEntity.getAgencyCode());
                        agencyAndId.setId(getTripId(brigadeTrip));

                        Trip trip = new Trip();
                        trip.setId(agencyAndId);
                        trip.setRoute(routesDictionary.get(brigadeTrip.getRouteId()));
                        trip.setServiceId(calendarDictionary.get(brigadeTrip.getBrigade().getCalendar().getCalendarName()).getServiceId());
                        return trip;
                    }).toList();

            var tripDictionary = brigadeTrips.stream().collect(Collectors.toMap(trip -> trip.getId().getId(), Function.identity()));

            brigadeTrips.forEach(writer::handleEntity);

            // handle StopTimes
            var stopTimes = brigadeTripEntities.stream()
                    .map(brigadeTrip -> gtfsStopTimeService.findAllByBrigadeTrip(brigadeTrip).stream()
                            .map(stopTime -> Pair.of(brigadeTrip, stopTime))
                            .toList())
                    .flatMap(Collection::stream)
                    .map(pair -> {
                        var brigadeTripEntity = pair.getLeft();
                        var stopTimeEntity = pair.getRight();

                        StopTime stopTime = new StopTime();
                        stopTime.setTrip(tripDictionary.get(getTripId(brigadeTripEntity)));

                        Stop stop = stopDictionary.get(stopTimeEntity.getStopEntity().getStopId());

                        stopTime.setStop(stop);
                        stopTime.setStopSequence(stopTimeEntity.getStopTimeId().getStopSequence());

                        LocalTime arrivalTime = LocalTime.ofSecondOfDay(0)
                                .plusSeconds(brigadeTripEntity.getDepartureTimeInSeconds())
                                .plusSeconds(stopTimeEntity.getArrivalSecond())
                                .truncatedTo(ChronoUnit.MINUTES);

                        LocalTime departureTime = LocalTime.ofSecondOfDay(0)
                                .plusSeconds(brigadeTripEntity.getDepartureTimeInSeconds())
                                .plusSeconds(stopTimeEntity.getDepartureSecond())
                                .truncatedTo(ChronoUnit.MINUTES);

                        stopTime.setArrivalTime(arrivalTime.toSecondOfDay());
                        stopTime.setDepartureTime(departureTime.toSecondOfDay());
                        stopTime.setTimepoint(1);
                        return stopTime;
                    })
                    .toList();

            stopTimes.forEach(writer::handleEntity);
            writer.close();

            File zippedGtfs = ZipUtils.zip(tempFile);

            return new ByteArrayResource(Files.readAllBytes(zippedGtfs.toPath())) {
                @Override
                public String getFilename() {
                    return "%s.gtfs.zip".formatted(agencyEntity.getAgencyCode());
                }
            };
        } catch (IOException e) {
        }

        return null;
    }

    private static String getTripId(BrigadeTripEntity brigadeTrip) {
        return brigadeTrip.getBrigade().getBrigadeNumber() + "/" + brigadeTrip.getTripSequence();
    }

    private static FeedInfo getFeedInfo(LocalDate now, LocalDate nowPlus30Days) {
        ServiceDate serviceDateNow = new ServiceDate(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        ServiceDate serviceDatePlus30Days = new ServiceDate(nowPlus30Days.getYear(), nowPlus30Days.getMonthValue(), nowPlus30Days.getDayOfMonth());

        FeedInfo feedInfo = new FeedInfo();
        feedInfo.setPublisherName("Przemysław Wrona");
        feedInfo.setPublisherUrl("https://www.nastepnastacja.pl");
        feedInfo.setContactUrl("https://www.nastepnastacja.pl");
        feedInfo.setStartDate(serviceDateNow);
        feedInfo.setEndDate(serviceDatePlus30Days);
        feedInfo.setLang("pl");
        feedInfo.setVersion("1.0.0");
        return feedInfo;
    }
}
