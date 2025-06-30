package pl.wrona.webserver.core.gtfs;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.GtfsApi;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class GtfsDownloadController implements GtfsApi {

    private final GtfsDownloadService gtfsDownloadService;

    @Override
    public ResponseEntity<Resource> downloadGtfs() {
        return ResponseEntity.ok(gtfsDownloadService.downloadGtfs());
    }
}
