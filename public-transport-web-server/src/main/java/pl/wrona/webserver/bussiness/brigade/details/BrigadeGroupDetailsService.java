package pl.wrona.webserver.bussiness.brigade.details;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeBodyV2;
import org.igeolab.iot.pt.server.api.model.BrigadeEvent;
import org.igeolab.iot.pt.server.api.model.BrigadeItemBody;
import org.igeolab.iot.pt.server.api.model.BrigadeResource;
import org.igeolab.iot.pt.server.api.model.CalendarItemId1;
import org.igeolab.iot.pt.server.api.model.CalendarSymbolId1;
import org.igeolab.iot.pt.server.api.model.GetBrigadeDetailsResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceQueryService;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeItemEntity;
import pl.wrona.webserver.core.brigade.BrigadeItemQueryRepository;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BrigadeGroupDetailsService {

    private final BrigadeItemQueryRepository brigadeItemQueryRepository;
    private final BrigadeGroupQueryService brigadeGroupQueryService;
    private final BrigadeResourceQueryService brigadeResourceQueryService;
    private final BrigadeEventQueryService brigadeEventQueryService;

    @PreAgencyAuthorize
    public BrigadeBodyV2 getCalendarSymbolBrigadeResources(String instance, String calendarCode, String symbol) {
        var brigadeGroup = brigadeGroupQueryService.findByCalendarCodeAndCalendarSymbol(instance, calendarCode, symbol);
        if (brigadeGroup == null) {
            return null;
        }

        var resourceEntities = brigadeResourceQueryService.findAllByBrigadeGroupId(brigadeGroup.getBrigadeGroupId());
        var eventsByResourceId = groupEventsByResourceId(resourceEntities);

        return new BrigadeBodyV2()
                .brigadeName(brigadeGroup.getName())
                .calendarSymbolId(mapCalendarSymbolId(brigadeGroup))
                .brigadeResources(mapResources(resourceEntities, eventsByResourceId));
    }

    @PreAgencyAuthorize
    public GetBrigadeDetailsResponse getBrigadeDetails(String instance, String brigadeCode) {
        var brigadeItem = brigadeItemQueryRepository.findByAgencyCodeAndSequenceHex(instance, brigadeCode);
        if (brigadeItem == null) {
            return null;
        }

        var brigadeGroups = brigadeGroupQueryService.findAllByBrigadeCode(instance, brigadeCode);
        var resourcesByGroupId = brigadeGroups.stream()
                .collect(Collectors.toMap(
                        BrigadeGroupEntity::getBrigadeGroupId,
                        group -> brigadeResourceQueryService.findAllByBrigadeGroupId(group.getBrigadeGroupId())));
        var eventsByResourceId = groupEventsByResourceId(
                resourcesByGroupId.values().stream().flatMap(List::stream).toList());

        return new GetBrigadeDetailsResponse()
                .brigade(mapItem(brigadeItem, brigadeGroups, resourcesByGroupId, eventsByResourceId));
    }

    private Map<Long, List<BrigadeEventEntity>> groupEventsByResourceId(List<BrigadeResourceEntity> resourceEntities) {
        return brigadeEventQueryService.findAllByResourceIds(
                        resourceEntities.stream().map(BrigadeResourceEntity::getBrigadeResourceId).toList())
                .stream()
                .collect(Collectors.groupingBy(event -> event.getResource().getBrigadeResourceId()));
    }

    private static BrigadeItemBody mapItem(
            BrigadeItemEntity brigadeItem,
            List<BrigadeGroupEntity> brigadeGroups,
            Map<Long, List<BrigadeResourceEntity>> resourcesByGroupId,
            Map<Long, List<BrigadeEventEntity>> eventsByResourceId) {
        return new BrigadeItemBody()
                .name(brigadeItem.getName())
                .sequence(brigadeItem.getSequence())
                .sequenceHex(brigadeItem.getSequenceHex())
                .brigades(brigadeGroups.stream()
                        .map(group -> mapGroup(
                                group,
                                resourcesByGroupId.getOrDefault(group.getBrigadeGroupId(), List.of()),
                                eventsByResourceId))
                        .toList());
    }

    private static BrigadeBodyV2 mapGroup(
            BrigadeGroupEntity brigadeGroupEntity,
            List<BrigadeResourceEntity> resourceEntities,
            Map<Long, List<BrigadeEventEntity>> eventsByResourceId) {
        return new BrigadeBodyV2()
                .brigadeName(brigadeGroupEntity.getName())
                .calendarSymbolId(mapCalendarSymbolId(brigadeGroupEntity))
                .calendarDesignation(brigadeGroupEntity.getCalendarSymbol().getDesignation())
                .calendarDescription(brigadeGroupEntity.getCalendarSymbol().getDescription())
                .brigadeResources(mapResources(resourceEntities, eventsByResourceId));
    }

    private static List<BrigadeResource> mapResources(
            List<BrigadeResourceEntity> resourceEntities,
            Map<Long, List<BrigadeEventEntity>> eventsByResourceId) {
        return resourceEntities.stream()
                .map(resource -> map(resource, eventsByResourceId.getOrDefault(resource.getBrigadeResourceId(), List.of())))
                .toList();
    }

    private static CalendarSymbolId1 mapCalendarSymbolId(BrigadeGroupEntity brigadeGroup) {
        return new CalendarSymbolId1()
                .calendarItemId(new CalendarItemId1()
                        .code(brigadeGroup.getCalendarSymbol().getCalendarItem().getSequenceHex()))
                .symbol(brigadeGroup.getCalendarSymbol().getDesignation());
    }

    private static BrigadeResource map(BrigadeResourceEntity brigadeResourceEntity, List<BrigadeEventEntity> events) {
        return new BrigadeResource()
                .sequence(brigadeResourceEntity.getSequence())
                .sequenceHex(brigadeResourceEntity.getSequenceHex())
                .events(events.stream().map(BrigadeGroupDetailsService::map).toList());
    }

    private static BrigadeEvent map(BrigadeEventEntity brigadeEventEntity) {
        return new BrigadeEvent()
                .startSecond(brigadeEventEntity.getStartSecond())
                .endSecond(brigadeEventEntity.getEndSecond())
                .line(brigadeEventEntity.getLine())
                .name(brigadeEventEntity.getName())
                .sequence(brigadeEventEntity.getSequence())
                .sequenceHex(brigadeEventEntity.getSequenceHex());
    }
}
