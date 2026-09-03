package pl.wrona.webserver.bussiness.brigade.event;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeResource;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.brigade.BrigadeEventQueryRepository;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;

import java.util.Collection;
import java.util.List;

@Service
@AllArgsConstructor
public class BrigadeEventQueryService {

    private final BrigadeEventQueryRepository brigadeEventQueryRepository;

    public List<BrigadeEventEntity> findAllByResourceIds(Collection<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return List.of();
        }
        return brigadeEventQueryRepository.findAllByResourceBrigadeResourceIdInOrderByStartSecondAsc(resourceIds);
    }

    public List<BrigadeEventEntity> findAllByAgencyAndBrigadeAndCalendarAndSymbol(String agency, String brigadeCode, String calendarCode, String calendarSymbol) {
        return brigadeEventQueryRepository.findAllByAgencyAndBrigadeAndCalendarAndSymbol(
                agency, brigadeCode, calendarCode, calendarSymbol);
    }

    public List<BrigadeEventEntity> findAllWithTripByAgencyAndBrigadeAndCalendarAndSymbol(String agency, String brigadeCode, String calendarCode, String calendarSymbol) {
        return brigadeEventQueryRepository.findAllWithTripByAgencyAndBrigadeAndCalendarAndSymbol(
                agency, brigadeCode, calendarCode, calendarSymbol);
    }

    public BrigadeEventEntity findByAgencyAndCalendarAndSymbolAndEventHex(BrigadeResourceEntity brigadeResource, String eventCode) {
        return brigadeEventQueryRepository.findByAgencyAndCalendarAndSymbolAndEventHex(brigadeResource, Hex.fromHex(eventCode));
    }
}
