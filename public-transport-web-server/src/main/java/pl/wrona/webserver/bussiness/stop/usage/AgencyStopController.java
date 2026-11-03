package pl.wrona.webserver.bussiness.stop.usage;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.AgencyStopsApi;
import org.igeolab.iot.pt.server.api.model.AgencyStops;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class AgencyStopController implements AgencyStopsApi {

    private final AgencyStopUsageService agencyStopUsageService;

    @Override
    public ResponseEntity<AgencyStops> getAgencyStops(String agency) {
        return ResponseEntity.ok(agencyStopUsageService.getAgencyStops(agency));
    }
}
