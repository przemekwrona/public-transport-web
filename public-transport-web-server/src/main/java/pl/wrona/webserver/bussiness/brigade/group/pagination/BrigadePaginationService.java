package pl.wrona.webserver.bussiness.brigade.group.pagination;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeBodyV2;
import org.igeolab.iot.pt.server.api.model.BrigadeItemBody;
import org.igeolab.iot.pt.server.api.model.CalendarItemId1;
import org.igeolab.iot.pt.server.api.model.CalendarSymbolId1;
import org.igeolab.iot.pt.server.api.model.GetBrigadeResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeItemEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BrigadePaginationService {

    private final BrigadeGroupQueryService brigadeGroupQueryService;

    @PreAgencyAuthorize
    public GetBrigadeResponse findBrigades(String instance) {
        var items = brigadeGroupQueryService.findAll(instance).stream()
                .collect(Collectors.groupingBy(
                        BrigadeGroupEntity::getBrigadeItem,
                        LinkedHashMap::new,
                        Collectors.toList()))
                .entrySet().stream()
                .map(entry -> mapItem(entry.getKey(), entry.getValue()))
                .toList();

        return new GetBrigadeResponse()
                .items(items);
    }

    private static BrigadeItemBody mapItem(BrigadeItemEntity brigadeItem, List<BrigadeGroupEntity> brigadeGroups) {
        return new BrigadeItemBody()
                .name(brigadeItem.getName())
                .sequence(brigadeItem.getSequence())
                .sequenceHex(brigadeItem.getSequenceHex())
                .brigades(brigadeGroups.stream()
                        .map(BrigadePaginationService::map)
                        .toList());
    }

    private static BrigadeBodyV2 map(BrigadeGroupEntity brigadeGroupEntity) {
        return new BrigadeBodyV2()
                .brigadeName(brigadeGroupEntity.getName())
                .calendarSymbolId(new CalendarSymbolId1()
                        .calendarItemId(new CalendarItemId1()
                                .code(brigadeGroupEntity.getCalendarSymbol().getCalendarItem().getSequenceHex()))
                        .symbol(brigadeGroupEntity.getCalendarSymbol().getDesignation()))
                .calendarDesignation(brigadeGroupEntity.getCalendarSymbol().getDesignation())
                .calendarDescription(brigadeGroupEntity.getCalendarSymbol().getDescription());
    }
}
