package pl.wrona.webserver.core.brigade;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.BrigadeApi;
import org.igeolab.iot.pt.server.api.model.BrigadeBody;
import org.igeolab.iot.pt.server.api.model.BrigadeDeleteBody;
import org.igeolab.iot.pt.server.api.model.BrigadePatchBody;
import org.igeolab.iot.pt.server.api.model.BrigadePayload;
import org.igeolab.iot.pt.server.api.model.GetBrigadeResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class BrigadeController implements BrigadeApi {

    private final BrigadeService brigadeService;

    @Override
    public ResponseEntity<GetBrigadeResponse> getBrigades(String agency) {
        return ResponseEntity.ok(brigadeService.findBrigades(agency));
    }

    @Override
    public ResponseEntity<Status> createBrigade(String agency, BrigadeBody createBrigadeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brigadeService.createBrigade(agency, createBrigadeRequest));
    }

    @Override
    public ResponseEntity<Status> deleteBrigade(String agency, BrigadeDeleteBody brigadeDeleteBody) {
        return ResponseEntity.status(HttpStatus.OK).body(brigadeService.deleteBrigade(agency, brigadeDeleteBody));
    }

    @Override
    public ResponseEntity<BrigadeBody> getBrigadeByBrigadeName(String agency, BrigadePayload brigadePayload) {
        return ResponseEntity.ok(brigadeService.getBrigadeByBrigadeName(agency, brigadePayload));
    }

    @Override
    public ResponseEntity<Status> updateBrigade(String agency, BrigadePatchBody brigadePatchBody) {
        return ResponseEntity.ok(brigadeService.updateBrigade(agency, brigadePatchBody));
    }
}
