package pl.wrona.webserver.bussiness.brigade.group.creator;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CreateCalendarSymbolBrigadeRequest;
import org.igeolab.iot.pt.server.api.model.CreateCalendarSymbolBrigadeResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceSequenceQueryService;
import pl.wrona.webserver.bussiness.calendar.CalendarSymbolQueryService;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceCommandService;
import pl.wrona.webserver.core.brigade.BrigadeGroupCommandRepository;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeGroupQueryRepository;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class BrigadeGroupCreatorService {

    private final CalendarSymbolQueryService calendarSymbolQueryService;
    private final BrigadeGroupCommandRepository brigadeGroupCommandRepository;
    private final BrigadeGroupQueryRepository brigadeGroupQueryRepository;
    private final BrigadeResourceSequenceQueryService brigadeResourceSequenceQueryService;
    private final BrigadeResourceCommandService brigadeResourceCommandService;

    @PreAgencyAuthorize
    @Transactional
    public CreateCalendarSymbolBrigadeResponse createBrigadeGroup(String instance, String calendarCode, String calendarSymbol, CreateCalendarSymbolBrigadeRequest request) {
        var calendarSymbolEntity = calendarSymbolQueryService.findByAgencyAndCalendarAndSymbol(instance, calendarCode, calendarSymbol);

        var brigadeGroup = brigadeGroupQueryRepository.findFirstByCalendarSymbol(calendarSymbolEntity);
        if (brigadeGroup != null) {
            return new CreateCalendarSymbolBrigadeResponse()
                    .status(new Status().status(Status.StatusEnum.EXISTS));
        }

        var brigadeGroupEntity = new BrigadeGroupEntity();
        brigadeGroupEntity.setCalendarSymbol(calendarSymbolEntity);
        brigadeGroupEntity.setName(request.getBrigadeName());
        var savedBrigadeGroup =  brigadeGroupCommandRepository.save(brigadeGroupEntity);

        var nextResourceSequence = this.brigadeResourceSequenceQueryService.findNextValue(instance, Hex.fromHex(calendarCode), calendarSymbol);
        var nextResourceSequenceCode = Hex.toHex(nextResourceSequence);

        var brigadeResourceEntity = new BrigadeResourceEntity();
        brigadeResourceEntity.setBrigadeGroup(savedBrigadeGroup);
        brigadeResourceEntity.setSequence(nextResourceSequence);
        brigadeResourceEntity.setSequenceHex(nextResourceSequenceCode);
        brigadeResourceEntity.setCreationDate(LocalDateTime.now());

        brigadeResourceCommandService.save(brigadeResourceEntity);

        return new CreateCalendarSymbolBrigadeResponse()
                .status(new Status().status(Status.StatusEnum.CREATED));
    }

}
