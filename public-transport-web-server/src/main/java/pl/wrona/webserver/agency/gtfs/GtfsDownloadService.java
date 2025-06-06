package pl.wrona.webserver.agency.gtfs;

import lombok.AllArgsConstructor;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.agency.AgencyService;
import pl.wrona.webserver.agency.RouteService;
import pl.wrona.webserver.agency.StopService;
import pl.wrona.webserver.agency.brigade.BrigadeTripService;
import pl.wrona.webserver.agency.calendar.CalendarDatesService;
import pl.wrona.webserver.agency.calendar.CalendarService;
import pl.wrona.webserver.agency.entity.Agency;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GtfsDownloadService {

    private final AgencyService agencyService;
    private final RouteService routeService;
    private final BrigadeTripService brigadeTripService;
    private final StopService stopService;

    private final CalendarService calendarService;
    private final CalendarDatesService calendarDatesService;

    public Resource downloadGtfs() {
        try {
            Path tempDir = Files.createTempDirectory("gtfs_");
            File tempFile = tempDir.toFile();

            GtfsWriter writer = new GtfsWriter();
            writer.setOutputLocation(tempFile);

            Agency agency = agencyService.getLoggedAgency();
            writer.handleEntity(AgencyHandler.handle(agency));

            var calendars = calendarService.findActiveCalendar().stream()
                    .map(CalendarHandler::handle)
                    .toList();

            calendars.forEach(writer::handleEntity);

            var calendarDictionary = calendars.stream()
                    .collect(Collectors.toMap(calendar -> calendar.getServiceId().getId(), Function.identity()));

            calendarDatesService.findAllActiveCalendarDate().stream()
                    .map(CalendarDateHandler::handle)
                    .forEach(writer::handleEntity);

            stopService.findAllStops(agency).stream()
                    .map(stop -> StopHandler.handle(agency, stop))
                    .forEach(writer::handleEntity);

            var routes = routeService.getRoutesEntities().stream()
                    .map(route -> RouteHandler.handle(agency, route))
                    .toList();

            routes.forEach(writer::handleEntity);

            var routesDictionary = routes.stream()
                    .collect(Collectors.toMap(route -> route.getId().getId(), Function.identity()));

            brigadeTripService.findAllBrigadeTripsForActiveCalendar().stream()
                    .map(brigadeTrip -> {
                        AgencyAndId agencyAndId = new AgencyAndId();
                        agencyAndId.setAgencyId(agency.getAgencyCode());
                        agencyAndId.setId(brigadeTrip.getBrigade().getBrigadeNumber() + "/" + brigadeTrip.getTripSequence());

                        Trip trip = new Trip();
                        trip.setId(agencyAndId);
                        trip.setRoute(routesDictionary.get(brigadeTrip.getLine()));
                        trip.setServiceId(calendarDictionary.get(brigadeTrip.getBrigade().getCalendar().getCalendarName()).getServiceId());
                        return trip;
                    }).forEach(writer::handleEntity);

            writer.close();

            File zippedGtfs = ZipUtils.zip(tempFile);

            return new ByteArrayResource(Files.readAllBytes(zippedGtfs.toPath())) {
                @Override
                public String getFilename() {
                    return "%s.gtfs.zip".formatted(agency.getAgencyCode());
                }
            };
        } catch (IOException e) {
        }

        return null;
    }
}
