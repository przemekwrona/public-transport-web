package pl.wrona.webserver.agency.brigade;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.BrigadeApi;
import org.igeolab.iot.pt.server.api.model.BrigadeBody;
import org.igeolab.iot.pt.server.api.model.BrigadePayload;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class BrigadeController implements BrigadeApi {

    private final BrigadeService brigadeService;

    @Override
    public ResponseEntity<Status> createBrigade(BrigadeBody createBrigadeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brigadeService.createBrigade(createBrigadeRequest));
    }

    @Override
    public ResponseEntity<BrigadeBody> getBrigadeByBrigadeName(BrigadePayload brigadePayload) {
        return ResponseEntity.ok(brigadeService.getBrigadeByBrigadeName(brigadePayload));
    }
}
