package pl.wrona.webserver.bussiness.brigade.event;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.brigade.BrigadeEventQueryRepository;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

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
                agency, Hex.fromHex(brigadeCode), Hex.fromHex(calendarCode), calendarSymbol);
    }

    public BrigadeEventEntity findByAgencyAndCalendarAndSymbolAndEventHex(BrigadeResourceEntity brigadeResource, String eventCode) {
        return brigadeEventQueryRepository.findByAgencyAndCalendarAndSymbolAndEventHex(brigadeResource, Hex.fromHex(eventCode));
    }

    public BrigadeEventEntity findByBrigadeGroupAndEventCode(BrigadeGroupEntity brigadeGroup, String eventCode) {
        return brigadeEventQueryRepository.findByBrigadeGroupAndEventCode(brigadeGroup, Hex.fromHex(eventCode));
    }
}
