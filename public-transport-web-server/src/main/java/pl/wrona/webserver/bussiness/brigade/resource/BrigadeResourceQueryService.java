package pl.wrona.webserver.bussiness.brigade.resource;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.core.brigade.BrigadeResourceQueryRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class BrigadeResourceQueryService {

    private final BrigadeResourceQueryRepository brigadeResourceQueryRepository;

    public BrigadeResourceEntity findById(Long brigadeResourceId) {
        return brigadeResourceQueryRepository.findById(brigadeResourceId).orElse(null);
    }

    public List<BrigadeResourceEntity> findAllByBrigadeGroupId(Long brigadeGroupId) {
        return brigadeResourceQueryRepository.findAllByBrigadeGroupBrigadeGroupIdOrderByResourceSequenceAsc(brigadeGroupId);
    }

    public BrigadeResourceEntity findByAgencyAndCalendarAndSymbolAndResourceCode(
            String agency, String brigadeItemCode, String calendarCode, String symbol, String resourceCode) {
        return brigadeResourceQueryRepository.findByAgencyAndCalendarAndSymbolAndResourceCode(
                agency, Hex.fromHex(brigadeItemCode), Hex.fromHex(calendarCode), symbol, Hex.fromHex(resourceCode));
    }

}
