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
import org.springframework.web.multipart.MultipartFile;
import pl.wrona.webserver.bussiness.admin.profile.creator.ProfileCreatorService;
import pl.wrona.webserver.bussiness.agency.details.AgencyDetailsService;
import pl.wrona.webserver.bussiness.agency.photo.AgencyPhotoService;
import pl.wrona.webserver.bussiness.agency.updater.AgencyUpdaterService;

import java.io.IOException;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class AgencyController implements AgencyApi {

    private final AgencyService agencyService;
    private final ProfileCreatorService profileCreatorService;
    private final AgencyDetailsService agencyDetailsService;
    private final AgencyUpdaterService agencyUpdaterService;
    private final AgencyPhotoService agencyPhotoService;

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
    public ResponseEntity getAgencyPhoto(String agency) throws IOException {
        return agencyPhotoService.getAgencyPhoto(agency);
    }

    @Override
    public ResponseEntity<Status> putAgencyPhoto(String agency, MultipartFile file) {
        return ResponseEntity.ok(agencyPhotoService.putAgencyPhoto(agency, file));
    }

    @Override
    public ResponseEntity<Status> updateAgency(String agency, AgencyDetails agencyDetails) {
        return ResponseEntity.ok(agencyUpdaterService.updateAgency(agency, agencyDetails));
    }
}
