package pl.wrona.webserver.bussiness.stop.usage;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.AgencyStopsApi;
import org.igeolab.iot.pt.server.api.model.AgencyStopNameRequest;
import org.igeolab.iot.pt.server.api.model.AgencyStops;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.StopsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.stop.rename.AgencyStopRenameService;
import pl.wrona.webserver.stops.BusStopService;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class AgencyStopController implements AgencyStopsApi {

    private final AgencyStopUsageService agencyStopUsageService;
    private final AgencyStopRenameService agencyStopRenameService;
    private final BusStopService busStopService;

    @Override
    public ResponseEntity<AgencyStops> getAgencyStops(String agency) {
        return ResponseEntity.ok(agencyStopUsageService.getAgencyStops(agency));
    }

    @Override
    public ResponseEntity<StopsResponse> getAgencyStopsInArea(String agency, Float maxLat, Float minLon, Float minLat, Float maxLon) {
        return ResponseEntity.ok(busStopService.findAgencyStopsInArea(agency, maxLat, minLon, minLat, maxLon));
    }

    @Override
    public ResponseEntity<StopsResponse> findAgencyStopsByStopName(String agency, String stopName) {
        return ResponseEntity.ok(busStopService.findAgencyStopsByStopName(agency, stopName));
    }

    @Override
    public ResponseEntity<Status> updateAgencyStopName(String agency, Long stopId, AgencyStopNameRequest agencyStopNameRequest) {
        return ResponseEntity.ok(agencyStopRenameService.updateAgencyStopName(agency, stopId, agencyStopNameRequest));
    }
}
