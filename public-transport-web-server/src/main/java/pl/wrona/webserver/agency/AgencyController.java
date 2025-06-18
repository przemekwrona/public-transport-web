package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.AgencyApi;
import org.igeolab.iot.pt.server.api.model.AgenciesAdminResponse;
import org.igeolab.iot.pt.server.api.model.AgencyAdminCreateAccountRequest;
import org.igeolab.iot.pt.server.api.model.AgencyDetails;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.admin.profile.creator.ProfileCreatorService;

@RestController
@AllArgsConstructor
public class AgencyController implements AgencyApi {

    private final AgencyService agencyService;
    private final ProfileCreatorService profileCreatorService;

    @Override
    public ResponseEntity<Status> createNewAccount(AgencyAdminCreateAccountRequest agencyAdminCreateAccountRequest) {
        return ResponseEntity.ok(profileCreatorService.createNewAccount(agencyAdminCreateAccountRequest));
    }

    @Override
    public ResponseEntity<AgenciesAdminResponse> findAllAgencies() {
        return ResponseEntity.ok(agencyService.findAllAgencies());
    }

    @Override
    public ResponseEntity<AgencyDetails> getAgency() {
        return ResponseEntity.ok(agencyService.getAgency());
    }

    @Override
    public ResponseEntity<Status> updateAgency(AgencyDetails agencyDetails) {
        return ResponseEntity.ok(agencyService.updateAgency(agencyDetails));
    }
}
