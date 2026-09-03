package pl.wrona.webserver.bussiness.brigade.group.creator;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CreateCalendarSymbolBrigadeRequest;
import org.igeolab.iot.pt.server.api.model.CreateCalendarSymbolBrigadeResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.bussiness.brigade.item.BrigadeItemQueryService;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceCommandService;
import pl.wrona.webserver.bussiness.calendar.CalendarSymbolQueryService;
import pl.wrona.webserver.core.brigade.BrigadeGroupCommandRepository;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeItemEntity;
import pl.wrona.webserver.core.brigade.BrigadeItemQueryRepository;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class BrigadeGroupCreatorService {

    private final BrigadeGroupCommandRepository brigadeGroupCommandRepository;
    private final CalendarSymbolQueryService calendarSymbolQueryService;
    private final BrigadeItemQueryService brigadeItemQueryService;
    private final BrigadeGroupQueryService brigadeGroupQueryService;
    private final BrigadeResourceCommandService brigadeResourceCommandService;

    @PreAgencyAuthorize
    @Transactional
    public CreateCalendarSymbolBrigadeResponse createBrigadeGroup(String instance, String brigadeCode, String calendarCode, String calendarSymbol, CreateCalendarSymbolBrigadeRequest request) {
        var usedCalendarSymbolEntity = calendarSymbolQueryService.findByAgencyAndBrigadeAndCalendarAndSymbol(instance, brigadeCode, calendarCode, calendarSymbol);

        if(usedCalendarSymbolEntity != null) {
            return new CreateCalendarSymbolBrigadeResponse()
                    .status(new Status().status(Status.StatusEnum.EXISTS));
        }

        var brigadeItemEntity = brigadeItemQueryService.findByBrigadeCode(instance, brigadeCode);
        var calendarSymbolEntity = calendarSymbolQueryService.findByAgencyAndBrigadeAndCalendarAndSymbol(instance, calendarCode, calendarSymbol);

        var brigadeGroup = new BrigadeGroupEntity();
        brigadeGroup.setBrigadeItem(brigadeItemEntity);
        brigadeGroup.setCalendarSymbol(calendarSymbolEntity);
        brigadeGroupCommandRepository.save(brigadeGroup);

        var savedBrigadeResource = brigadeResourceCommandService.init(instance, brigadeCode, calendarCode, calendarSymbol);

        return new CreateCalendarSymbolBrigadeResponse()
                .status(new Status().status(Status.StatusEnum.CREATED));
    }

}
