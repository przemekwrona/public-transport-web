package pl.wrona.webserver.bussiness.brigade.event;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.brigade.BrigadeEventQueryRepository;

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
        return brigadeEventQueryRepository.findAllByAgencyAndBrigadeAndCalendarAndSymbol(agency, brigadeCode, calendarCode, calendarSymbol);
    }

    public BrigadeEventEntity findByAgencyAndCalendarAndSymbolAndEventHex(String agency, String calendarCode, String symbol, String eventCode) {
        return brigadeEventQueryRepository.findByAgencyAndCalendarAndSymbolAndEventHex(agency, calendarCode, symbol, eventCode);
    }
}
