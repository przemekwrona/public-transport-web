package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.GoogleAgreementsApi;
import org.igeolab.iot.pt.server.api.model.GoogleAgreementsRequest;
import org.igeolab.iot.pt.server.api.model.GoogleAgreementsResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class GoogleAgreementsController implements GoogleAgreementsApi {

    private final GoogleAgreementsService googleAgreementsService;

    @Override
    public ResponseEntity<GoogleAgreementsResponse> getGoogleAgreements() {
        return ResponseEntity.ok(googleAgreementsService.getGoogleAgreements());
    }

    @Override
    public ResponseEntity<Status> updateGoogleAgreements(GoogleAgreementsRequest googleAgreementsRequest) {
        return ResponseEntity.ok(googleAgreementsService.updateGoogleAgreements(googleAgreementsRequest));
    }
}
