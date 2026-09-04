package pl.wrona.webserver.bussiness.brigade.details;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeEvent;
import org.igeolab.iot.pt.server.api.model.BrigadeGroupBody;
import org.igeolab.iot.pt.server.api.model.BrigadeItemBody;
import org.igeolab.iot.pt.server.api.model.BrigadeResource;
import org.igeolab.iot.pt.server.api.model.CalendarItemId1;
import org.igeolab.iot.pt.server.api.model.CalendarSymbolId1;
import org.igeolab.iot.pt.server.api.model.GetBrigadeDetailsResponse;
import org.igeolab.iot.pt.server.api.model.RouteId1;
import org.igeolab.iot.pt.server.api.model.TripId2;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceQueryService;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeItemEntity;
import pl.wrona.webserver.core.brigade.BrigadeItemQueryRepository;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.core.mapper.TripTrafficModeMapper;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;
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
    public BrigadeGroupBody getCalendarSymbolBrigadeResources(String instance, String brigadeCode) {
        var brigadeGroup = brigadeGroupQueryService.findByBrigadeCode(instance, brigadeCode);
        if (brigadeGroup == null) {
            return null;
        }

        var resourceEntities = brigadeResourceQueryService.findAllByBrigadeGroupId(brigadeGroup.getBrigadeGroupId());
        var eventsByResourceId = groupEventsByResourceId(resourceEntities);

        return new BrigadeGroupBody()
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
                .brigadeName(brigadeItem.getName())
                .defaultRouteCode(brigadeItem.getDefaultRoute().getRouteCode())
                .sequence(brigadeItem.getBrigadeItemSequence())
                .sequenceHex(brigadeItem.getBrigadeItemCode())
                .brigades(brigadeGroups.stream()
                        .map(group -> mapGroup(
                                group,
                                resourcesByGroupId.getOrDefault(group.getBrigadeGroupId(), List.of()),
                                eventsByResourceId))
                        .toList());
    }

    private static BrigadeGroupBody mapGroup(
            BrigadeGroupEntity brigadeGroupEntity,
            List<BrigadeResourceEntity> resourceEntities,
            Map<Long, List<BrigadeEventEntity>> eventsByResourceId) {
        return new BrigadeGroupBody()
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
                .sequence(brigadeResourceEntity.getResourceSequence())
                .sequenceHex(brigadeResourceEntity.getResourceCode())
                .events(events.stream().map(BrigadeGroupDetailsService::map).toList());
    }

    private static BrigadeEvent map(BrigadeEventEntity brigadeEventEntity) {
        return new BrigadeEvent()
                .startSecond(brigadeEventEntity.getStartSecond())
                .endSecond(brigadeEventEntity.getEndSecond())
                .line(brigadeEventEntity.getLine())
                .name(brigadeEventEntity.getName())
                .sequence(brigadeEventEntity.getEventSequence())
                .sequenceHex(brigadeEventEntity.getEventCode())
                .tripId(mapTripId(brigadeEventEntity.getTripProfile()));
    }

    private static TripId2 mapTripId(TripProfileEntity tripProfile) {
        if (tripProfile == null || tripProfile.getTrip() == null || tripProfile.getTrip().getRoute() == null) {
            return null;
        }
        var trip = tripProfile.getTrip();
        return new TripId2()
                .routeId(new RouteId1()
                        .line(trip.getRoute().getLine())
                        .name(trip.getRoute().getName())
                        .version(trip.getRoute().getVersion())
                        .routeCode(trip.getRoute().getRouteCode()))
                .tripCode(trip.getTripCode())
                .variantName(trip.getVariantName())
                .variantMode(TripVariantModeMapper.map(trip.getVariantMode()))
                .trafficMode(TripTrafficModeMapper.map(tripProfile.getTrafficMode()));
    }
}
