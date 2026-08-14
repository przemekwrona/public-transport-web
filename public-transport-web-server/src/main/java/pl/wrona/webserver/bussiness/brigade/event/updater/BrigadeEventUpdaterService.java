package pl.wrona.webserver.bussiness.brigade.event.updater;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.PutBrigadeEventBody;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceQueryService;
import pl.wrona.webserver.core.brigade.BrigadeEventCommandRepository;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class BrigadeEventUpdaterService {

    private final BrigadeEventCommandRepository brigadeEventCommandRepository;
    private final BrigadeResourceQueryService brigadeResourceQueryService;

    @PreAgencyAuthorize
    @Transactional
    public Status putBrigadeEvent(String instance, String calendarCode, String symbol, String resourceCode, PutBrigadeEventBody putBrigadeEventBody) {
        var resource = brigadeResourceQueryService.findByAgencyAndCalendarAndSymbolAndResourceCode(
                instance, calendarCode, symbol, resourceCode);

        var brigadeEvent = new BrigadeEventEntity();
        brigadeEvent.setStartSecond(putBrigadeEventBody.getStartSecond());
        brigadeEvent.setEndSecond(putBrigadeEventBody.getEndSecond());
        brigadeEvent.setResource(resource);
        brigadeEvent.setLine(putBrigadeEventBody.getLine());
        brigadeEvent.setName(putBrigadeEventBody.getName());
        brigadeEvent.setSequence(putBrigadeEventBody.getSequence());
        brigadeEvent.setSequenceHex(putBrigadeEventBody.getSequenceHex());

        brigadeEventCommandRepository.save(brigadeEvent);

        return new Status().status(Status.StatusEnum.SUCCESS);
    }

}
