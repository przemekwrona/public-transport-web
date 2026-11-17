package pl.wrona.webserver.bussiness.subdivision;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.SubdivisionApi;
import org.igeolab.iot.pt.server.api.model.SubdivisionByStopsRequest;
import org.igeolab.iot.pt.server.api.model.SubdivisionByStopsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class SubdivisionController implements SubdivisionApi {

    private final SubdivisionQueryService subdivisionQueryService;

    @Override
    public ResponseEntity<SubdivisionByStopsResponse> findSubdivisionsByStopIds(
            SubdivisionByStopsRequest subdivisionByStopsRequest) {
        return ResponseEntity.ok(
                subdivisionQueryService.findSubdivisionsByStopIds(subdivisionByStopsRequest.getStopIds()));
    }
}
