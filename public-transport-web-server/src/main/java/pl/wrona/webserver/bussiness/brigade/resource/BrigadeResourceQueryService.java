package pl.wrona.webserver.bussiness.brigade.resource;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.core.brigade.BrigadeResourceQueryRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class BrigadeResourceQueryService {

    private final BrigadeResourceQueryRepository brigadeResourceQueryRepository;

    public List<BrigadeResourceEntity> findAllByBrigadeGroupId(Long brigadeGroupId) {
        return brigadeResourceQueryRepository.findAllByBrigadeGroupBrigadeGroupIdOrderByResourceSequenceAsc(brigadeGroupId);
    }

    public List<BrigadeResourceEntity> findByBrigadeGroup(BrigadeGroupEntity group) {
        return brigadeResourceQueryRepository.findByAgencyAndCalendar(group);
    }

    public BrigadeResourceEntity findByBrigadeGroupAndResourceCode(BrigadeGroupEntity group, String resourceCode) {
        return brigadeResourceQueryRepository.findByAgencyAndCalendarAndSymbolAndResourceCode(group, Hex.fromHex(resourceCode));
    }

    public BrigadeResourceEntity findByAgencyAndBrigadeCodeAndCalendarCodeAndSymbolAndResourceCode(
            String agency, String brigadeItemCode, String calendarCode, String symbol, String resourceCode) {
        return brigadeResourceQueryRepository.findByAgencyAndCalendarAndSymbolAndResourceCode(
                agency, Hex.fromHex(brigadeItemCode), Hex.fromHex(calendarCode), symbol, Hex.fromHex(resourceCode));
    }

}
