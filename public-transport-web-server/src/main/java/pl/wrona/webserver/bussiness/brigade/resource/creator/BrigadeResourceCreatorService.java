package pl.wrona.webserver.bussiness.brigade.resource.creator;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.NextCalendarResourceSequenceResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceCommandService;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class BrigadeResourceCreatorService {

    private final BrigadeResourceCommandService brigadeResourceCommandService;

    @PreAgencyAuthorize
    @Transactional
    public NextCalendarResourceSequenceResponse getNextCalendarResourceSequence(String instance, String calendarCode, String symbol) {
        var resource = brigadeResourceCommandService.init(instance, calendarCode, symbol);
        return new NextCalendarResourceSequenceResponse()
                .sequence(resource.getSequence())
                .sequenceHex(resource.getSequenceHex());
    }

}
