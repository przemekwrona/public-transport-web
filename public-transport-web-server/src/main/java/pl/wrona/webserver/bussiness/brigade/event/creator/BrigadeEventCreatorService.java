package pl.wrona.webserver.bussiness.brigade.event.creator;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.NextBrigadeEventSequenceResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventSequenceQueryService;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class BrigadeEventCreatorService {

    private final BrigadeEventSequenceQueryService brigadeEventSequenceQueryService;

    @PreAgencyAuthorize
    @Transactional
    public NextBrigadeEventSequenceResponse getNextBrigadeEventSequence(String instance, String calendarCode, String symbol, String resourceCode) {
        var nextSequence = brigadeEventSequenceQueryService.findNextValue(instance, calendarCode, symbol);
        return new NextBrigadeEventSequenceResponse()
                .sequence(nextSequence)
                .sequenceHex(Hex.toHex(nextSequence));
    }

}
