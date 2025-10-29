package pl.wrona.webserver.core;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.AgencyApi;
import org.igeolab.iot.pt.server.api.model.AgenciesAdminResponse;
import org.igeolab.iot.pt.server.api.model.AgencyAdminCreateAccountRequest;
import org.igeolab.iot.pt.server.api.model.AgencyDetails;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.admin.profile.creator.ProfileCreatorService;
import pl.wrona.webserver.bussiness.agency.details.AgencyDetailsService;
import pl.wrona.webserver.bussiness.agency.updater.AgencyUpdaterService;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class AgencyController implements AgencyApi {

    private final AgencyService agencyService;
    private final ProfileCreatorService profileCreatorService;
    private final AgencyDetailsService agencyDetailsService;
    private final AgencyUpdaterService agencyUpdaterService;

    @Override
    public ResponseEntity<Status> createNewAccount(AgencyAdminCreateAccountRequest agencyAdminCreateAccountRequest) {
        return ResponseEntity.ok(profileCreatorService.createNewAccount(agencyAdminCreateAccountRequest));
    }

    @Override
    public ResponseEntity<AgenciesAdminResponse> findAllAgencies() {
        return ResponseEntity.ok(agencyService.findAllAgencies());
    }

    @Override
    public ResponseEntity<AgencyDetails> getAgency(String agency) {
        return ResponseEntity.ok(agencyDetailsService.getAgency(agency));
    }

    @Override
    public ResponseEntity<Status> updateAgency(String agency, AgencyDetails agencyDetails) {
        return ResponseEntity.ok(agencyUpdaterService.updateAgency(agency, agencyDetails));
    }
}
