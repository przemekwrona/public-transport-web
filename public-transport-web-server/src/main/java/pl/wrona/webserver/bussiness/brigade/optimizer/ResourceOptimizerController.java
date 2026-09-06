package pl.wrona.webserver.bussiness.brigade.optimizer;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.ResourceOptimizerApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class ResourceOptimizerController implements ResourceOptimizerApi {

    private final OrToolsResourceOptimizerService orToolsResourceOptimizerService;

    @Override
    public ResponseEntity<Void> optimizeBrigades(String agency, String brigadeCode, String calendarCode, String symbol) {
        orToolsResourceOptimizerService.optimizeBrigades(agency, brigadeCode, calendarCode, symbol);
        return ResponseEntity.ok().build();
    }
}
