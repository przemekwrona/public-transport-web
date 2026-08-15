package pl.wrona.webserver.bussiness.brigade.resource;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.core.brigade.BrigadeResourceCommandRepository;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class BrigadeResourceCommandService {

    private final BrigadeResourceCommandRepository brigadeResourceCommandRepository;
    private final BrigadeResourceSequenceQueryService brigadeResourceSequenceQueryService;
    private final BrigadeGroupQueryService brigadeGroupQueryService;

    @Transactional
    public BrigadeResourceEntity save(BrigadeResourceEntity brigadeGroupEntity) {
        return brigadeResourceCommandRepository.save(brigadeGroupEntity);
    }

    @Transactional
    public BrigadeResourceEntity init(String instance, Integer brigadeItemSequence, String calendarCode, String calendarSymbol) {
        var nextResourceSequence = this.brigadeResourceSequenceQueryService.findNextValue(instance, brigadeItemSequence, Hex.fromHex(calendarCode), calendarSymbol);
        var nextResourceSequenceCode = Hex.toHex(nextResourceSequence);

        var savedBrigadeGroup = brigadeGroupQueryService.findByCalendarCodeAndCalendarSymbol(instance, calendarCode, calendarSymbol);

        var brigadeResourceEntity = new BrigadeResourceEntity();
        brigadeResourceEntity.setBrigadeGroup(savedBrigadeGroup);
        brigadeResourceEntity.setSequence(nextResourceSequence);
        brigadeResourceEntity.setSequenceHex(nextResourceSequenceCode);
        brigadeResourceEntity.setCreationDate(LocalDateTime.now());

        return save(brigadeResourceEntity);
    }
}
