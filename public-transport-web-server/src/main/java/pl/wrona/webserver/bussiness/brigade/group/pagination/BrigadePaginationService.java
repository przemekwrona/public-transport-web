package pl.wrona.webserver.bussiness.brigade.group.pagination;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CalendarItemId1;
import org.igeolab.iot.pt.server.api.model.CalendarSymbolId1;
import org.igeolab.iot.pt.server.api.model.GetBrigadeBody;
import org.igeolab.iot.pt.server.api.model.GetBrigadeResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class BrigadePaginationService {

    private final BrigadeGroupQueryService brigadeGroupQueryService;

    @PreAgencyAuthorize
    public GetBrigadeResponse findBrigades(String instance) {
        var brigades = brigadeGroupQueryService.findAll(instance).stream()
                .map(BrigadePaginationService::map)
                .toList();

        return new GetBrigadeResponse()
                .brigades(brigades);
    }

    private static GetBrigadeBody map(BrigadeGroupEntity brigadeGroupEntity) {
        return new GetBrigadeBody()
                .brigadeName(brigadeGroupEntity.getName())
                .calendarSymbolId(new CalendarSymbolId1()
                        .calendarItemId(new CalendarItemId1()
                                .code(brigadeGroupEntity.getCalendarSymbol().getCalendarItem().getSequenceHex()))
                        .symbol(brigadeGroupEntity.getCalendarSymbol().getDesignation()))
                .calendarDesignation(brigadeGroupEntity.getCalendarSymbol().getDesignation())
                .calendarDescription(brigadeGroupEntity.getCalendarSymbol().getDescription());
    }
}
