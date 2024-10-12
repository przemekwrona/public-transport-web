package pl.wrona.webserver.summary;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.SummaryApi;
import org.igeolab.iot.pt.server.api.model.JourneySummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class JourneySummaryController implements SummaryApi {

    private final JourneySummaryService journeySummaryService;

    @Override
    public ResponseEntity<JourneySummaryResponse> summaryTrip(String instanceId, String fromPlace, String toPlace, String date, String time, String mode, String local, Boolean showIntermediateStops, Double maxWalkDistance, Boolean arriveBy, Boolean wheelchair, Integer numItineraries, Boolean realtime, String optimize) {
        return ResponseEntity.ok(journeySummaryService.summaryTrip(instanceId, fromPlace, toPlace, date, time, mode, local, showIntermediateStops, maxWalkDistance, arriveBy, wheelchair, numItineraries, realtime, optimize));
    }
}
