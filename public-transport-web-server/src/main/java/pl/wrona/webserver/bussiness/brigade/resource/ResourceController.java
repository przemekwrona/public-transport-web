package pl.wrona.webserver.bussiness.brigade.resource;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.ResourceApi;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.brigade.resource.deletion.ResourceDeletionService;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class ResourceController implements ResourceApi {

    private final ResourceDeletionService resourceDeletionService;

    @Override
    public ResponseEntity<Status> deleteResource(String agency, String brigadeCode, String calendarCode, String symbol) {
        return ResponseEntity.ok(resourceDeletionService.deleteResourceAndAppendOne(agency, brigadeCode, calendarCode, symbol));
    }
}
