package pl.wrona.webserver.agency.gtfs;

import lombok.AllArgsConstructor;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.agency.AgencyService;
import pl.wrona.webserver.agency.RouteService;
import pl.wrona.webserver.agency.StopService;
import pl.wrona.webserver.agency.entity.Agency;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@AllArgsConstructor
public class GtfsDownloadService {

    private final AgencyService agencyService;
    private final RouteService routeService;
    private final StopService stopService;

    private final CalendarService calendarService;

    public Resource downloadGtfs() {
        try {
            Path tempDir = Files.createTempDirectory("gtfs_");
            File tempFile = tempDir.toFile();

            GtfsWriter writer = new GtfsWriter();
            writer.setOutputLocation(tempFile);

            Agency agency = agencyService.getLoggedAgency();
            writer.handleEntity(AgencyHandler.handle(agency));

            calendarService.findActiveCalendar().stream()
                    .map(CalendarHandler::handle)
                    .forEach(writer::handleEntity);

            routeService.getRoutesEntities().stream()
                    .map(route -> RouteHandler.handle(agency, route))
                    .forEach(writer::handleEntity);

            stopService.findAllStops(agency).stream()
                    .map(stop -> StopHandler.handle(agency, stop))
                    .forEach(writer::handleEntity);

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
