package pl.wrona.webserver.bussiness.location;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.LocationApi;
import org.igeolab.iot.pt.server.api.model.LocationSearchResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class LocationController implements LocationApi {

    private final LocationQueryService locationQueryService;

    @Override
    public ResponseEntity<LocationSearchResponse> findLocationByName(String q) {
        return ResponseEntity.ok(locationQueryService.findLocationByName(q));
    }
}
