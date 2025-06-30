package pl.wrona.webserver.core.gtfs;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import pl.wrona.webserver.core.StopTimeService;
import pl.wrona.webserver.core.agency.RouteQueryService;
import pl.wrona.webserver.core.brigade.BrigadeTripService;
import pl.wrona.webserver.core.calendar.CalendarDatesService;
import pl.wrona.webserver.core.calendar.CalendarService;
import pl.wrona.webserver.core.agency.AgencyEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class GtfsDownloadService {

    private final AgencyService agencyService;
//    private final RouteService routeService;
    private final RouteQueryService routeQueryService;
    private final BrigadeTripService brigadeTripService;
    private final StopService stopService;
    private final StopTimeService stopTimeService;

    private final CalendarService calendarService;
    private final CalendarDatesService calendarDatesService;

    public Resource downloadGtfs() {
        try {
            Path tempDir = Files.createTempDirectory("gtfs_");
            File tempFile = tempDir.toFile();

            GtfsWriter writer = new GtfsWriter();
            writer.setOutputLocation(tempFile);

            LocalDate now = LocalDate.now();
            LocalDate nowPlus30Days = LocalDate.now().plusDays(30);

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

            writer.handleEntity(feedInfo);

            AgencyEntity agencyEntity = agencyService.getLoggedAgency();
            writer.handleEntity(AgencyHandler.handle(agencyEntity));

            var calendars = calendarService.findActiveCalendar().stream()
                    .map(CalendarHandler::handle)
                    .toList();

            calendars.forEach(writer::handleEntity);

            var calendarDictionary = calendars.stream()
                    .collect(Collectors.toMap(calendar -> calendar.getServiceId().getId(), Function.identity()));

            calendarDatesService.findAllActiveCalendarDate().stream()
                    .map(CalendarDateHandler::handle)
                    .forEach(writer::handleEntity);

            var stops = stopService.findAllStops(agencyEntity).stream()
                    .map(stop -> StopHandler.handle(agencyEntity, stop))
                    .toList();

            stops.forEach(writer::handleEntity);

            var stopDictionary = stops.stream().collect(Collectors.toMap(stop -> Long.parseLong(stop.getId().getId()), Function.identity()));

            var routes = routeQueryService.findRouteByAgencyCode(agencyEntity.getAgencyCode()).stream()
                    .map(route -> RouteHandler.handle(agencyEntity, route))
                    .toList();

            routes.forEach(writer::handleEntity);

            var routesDictionary = routes.stream()
                    .collect(Collectors.toMap(route -> route.getId().getId(), Function.identity()));

            var trips = brigadeTripService.findAllBrigadeTripsForActiveCalendar().stream()
                    .map(brigadeTrip -> {
                        AgencyAndId agencyAndId = new AgencyAndId();
                        agencyAndId.setAgencyId(agencyEntity.getAgencyCode());
                        agencyAndId.setId(brigadeTrip.getBrigade().getBrigadeNumber() + "/" + brigadeTrip.getTripSequence());

                        Trip trip = new Trip();
                        trip.setId(agencyAndId);
                        trip.setRoute(routesDictionary.get(brigadeTrip.getRouteId()));
                        trip.setServiceId(calendarDictionary.get(brigadeTrip.getBrigade().getCalendar().getCalendarName()).getServiceId());
                        return trip;
                    }).toList();

            var tripDictionary = trips.stream().collect(Collectors.toMap(trip -> trip.getId().getId(), Function.identity()));

            trips.forEach(writer::handleEntity);

            List<StopTime> a = brigadeTripService.findAllBrigadeTripsForActiveCalendar().stream()
                    .map(brigadeTrip -> {
                        String tripId = "%s/%s".formatted(brigadeTrip.getBrigade().getBrigadeNumber(), brigadeTrip.getTripSequence());

                        return stopTimeService.getAllStopTimesByTrip(brigadeTrip.getRootTrip()).stream()
                                .map(stop -> {
                                    StopTime stopTime = new StopTime();
                                    stopTime.setTrip(tripDictionary.get(tripId));

                                    Long idd = stop.getStopEntity().getStopId();
                                    Stop stop1 = stopDictionary.get(idd);

                                    stopTime.setStop(stop1);
                                    stopTime.setStopSequence(stop.getStopTimeId().getStopSequence());


                                    LocalTime arrivalTime = LocalTime.ofSecondOfDay(0)
                                            .plusSeconds(brigadeTrip.getDepartureTimeInSeconds())
                                            .plusSeconds(stop.getArrivalSecond())
                                            .truncatedTo(ChronoUnit.MINUTES);

                                    LocalTime departureTime = LocalTime.ofSecondOfDay(0)
                                            .plusSeconds(brigadeTrip.getDepartureTimeInSeconds())
                                            .plusSeconds(stop.getDepartureSecond())
                                            .truncatedTo(ChronoUnit.MINUTES);

                                    stopTime.setArrivalTime(arrivalTime.toSecondOfDay());
                                    stopTime.setDepartureTime(departureTime.toSecondOfDay());
                                    stopTime.setTimepoint(1);
                                    return stopTime;
                                })
                                .toList();

                    })
                    .flatMap(Collection::stream)
                    .toList();

            a.forEach(writer::handleEntity);

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
}
