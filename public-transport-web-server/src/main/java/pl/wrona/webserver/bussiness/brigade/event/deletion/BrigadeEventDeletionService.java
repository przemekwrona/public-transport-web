package pl.wrona.webserver.bussiness.brigade.event.deletion;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.core.brigade.BrigadeEventCommandRepository;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class BrigadeEventDeletionService {

    private final BrigadeEventCommandRepository brigadeEventCommandRepository;
    private final BrigadeEventQueryService brigadeEventQueryService;

    @PreAgencyAuthorize
    @Transactional
    public Status deleteBrigadeEvent(String instance, String calendarCode, String symbol, String eventCode) {
        BrigadeEventEntity brigadeEvent = brigadeEventQueryService.findByAgencyAndCalendarAndSymbolAndEventHex(
                instance, calendarCode, symbol, eventCode);

        if (brigadeEvent != null) {
            brigadeEventCommandRepository.delete(brigadeEvent);
        }

        return new Status().status(Status.StatusEnum.DELETED);
    }
}
