package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.AgencyApi;
import org.igeolab.iot.pt.server.api.model.AgencyDetails;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AgencyController implements AgencyApi {

    private final AgencyService agencyService;

    @Override
    public ResponseEntity<AgencyDetails> getAgency() {
        return ResponseEntity.ok(agencyService.getAgency());
    }

    @Override
    public ResponseEntity<Status> updateAgency(AgencyDetails agencyDetails) {
        return ResponseEntity.ok(agencyService.updateAgency(agencyDetails));
    }
}
