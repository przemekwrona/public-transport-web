package pl.wrona.webserver.bussiness.brigade.item.pagination;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeBodyV2;
import org.igeolab.iot.pt.server.api.model.BrigadeItemBody;
import org.igeolab.iot.pt.server.api.model.CalendarItemId1;
import org.igeolab.iot.pt.server.api.model.CalendarSymbolId1;
import org.igeolab.iot.pt.server.api.model.GetBrigadeResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.bussiness.brigade.item.BrigadeItemQueryService;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeItemEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BrigadeItemPaginationService {

    private final BrigadeGroupQueryService brigadeGroupQueryService;
    private final BrigadeItemQueryService brigadeItemQueryService;

    @PreAgencyAuthorize
    public GetBrigadeResponse findBrigades(String instance) {
        Map<Long, List<BrigadeGroupEntity>> groupsByItemId = brigadeGroupQueryService.findAll(instance).stream()
                .collect(Collectors.groupingBy(
                        group -> group.getBrigadeItem().getBrigadeItemId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        var items = brigadeItemQueryService.findAll(instance).stream()
                .map(item -> mapItem(item, groupsByItemId.getOrDefault(item.getBrigadeItemId(), List.of())))
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
                        .map(BrigadeItemPaginationService::map)
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
