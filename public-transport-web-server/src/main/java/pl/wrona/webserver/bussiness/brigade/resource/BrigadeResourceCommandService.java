package pl.wrona.webserver.bussiness.brigade.resource;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.NextCalendarResourceSequenceResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.core.brigade.BrigadeResourceCommandRepository;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

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

    @PreAgencyAuthorize
    @Transactional
    public NextCalendarResourceSequenceResponse getNextCalendarResourceSequence(String instance, String brigadeCode, String calendarCode, String calendarSymbol) {
        var savedResource = init(instance, brigadeCode, calendarCode, calendarSymbol);
        return new NextCalendarResourceSequenceResponse()
                .sequence(savedResource.getSequence())
                .sequenceHex(savedResource.getSequenceHex());
    }

    @Transactional
    public BrigadeResourceEntity init(String instance, String brigadeCode, String calendarCode, String calendarSymbol) {
        var nextResourceSequence = this.brigadeResourceSequenceQueryService.findNextValue(instance, brigadeCode, Hex.fromHex(calendarCode), calendarSymbol);
        var nextResourceSequenceCode = Hex.toHex(nextResourceSequence);

        var savedBrigadeGroup = brigadeGroupQueryService.findByBrigadeCode(instance, brigadeCode, calendarCode, calendarSymbol);

        var brigadeResourceEntity = new BrigadeResourceEntity();
        brigadeResourceEntity.setBrigadeGroup(savedBrigadeGroup);
        brigadeResourceEntity.setSequence(nextResourceSequence);
        brigadeResourceEntity.setSequenceHex(nextResourceSequenceCode);
        brigadeResourceEntity.setCreationDate(LocalDateTime.now());

        return save(brigadeResourceEntity);
    }
}
