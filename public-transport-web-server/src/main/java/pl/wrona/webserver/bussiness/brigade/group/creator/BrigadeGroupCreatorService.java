package pl.wrona.webserver.bussiness.brigade.group.creator;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CreateCalendarSymbolBrigadeRequest;
import org.igeolab.iot.pt.server.api.model.CreateCalendarSymbolBrigadeResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceCommandService;
import pl.wrona.webserver.bussiness.calendar.CalendarSymbolQueryService;
import pl.wrona.webserver.core.brigade.BrigadeGroupCommandRepository;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class BrigadeGroupCreatorService {

    private final BrigadeGroupCommandRepository brigadeGroupCommandRepository;
    private final CalendarSymbolQueryService calendarSymbolQueryService;
    private final BrigadeGroupQueryService brigadeGroupQueryService;
    private final BrigadeResourceCommandService brigadeResourceCommandService;

    @PreAgencyAuthorize
    @Transactional
    public CreateCalendarSymbolBrigadeResponse createBrigadeGroup(String instance, String brigadeCode, String calendarCode, String calendarSymbol, CreateCalendarSymbolBrigadeRequest request) {
        var calendarSymbolEntity = calendarSymbolQueryService.findByAgencyAndCalendarAndSymbol(instance, calendarCode, calendarSymbol);

        var brigadeGroup = brigadeGroupQueryService.findByCalendarCodeAndCalendarSymbol(instance, calendarCode, calendarSymbol);
        if (brigadeGroup != null) {
            return new CreateCalendarSymbolBrigadeResponse()
                    .status(new Status().status(Status.StatusEnum.EXISTS));
        }

        var brigadeGroupEntity = new BrigadeGroupEntity();
        brigadeGroupEntity.setCalendarSymbol(calendarSymbolEntity);
        brigadeGroupEntity.setName(request.getBrigadeName());
        brigadeGroupCommandRepository.save(brigadeGroupEntity);

        brigadeResourceCommandService.init(instance, brigadeCode, calendarCode, calendarSymbol);

        return new CreateCalendarSymbolBrigadeResponse()
                .status(new Status().status(Status.StatusEnum.CREATED));
    }

}
