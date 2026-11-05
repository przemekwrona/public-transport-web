package pl.wrona.webserver.bussiness.stop.timetable;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.AgencyStopTimetableApi;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class AgencyStopTimetableController implements AgencyStopTimetableApi {

    private final AgencyStopTimetableService agencyStopTimetableService;

    @Override
    public ResponseEntity<Resource> downloadAgencyStopTimetable(String agency, Long stopId) {
        return agencyStopTimetableService.downloadAgencyStopTimetable(agency, stopId);
    }
}
