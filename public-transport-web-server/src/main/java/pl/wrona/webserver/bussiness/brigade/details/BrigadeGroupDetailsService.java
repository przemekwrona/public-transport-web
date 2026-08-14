package pl.wrona.webserver.bussiness.brigade.details;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeBody;
import org.igeolab.iot.pt.server.api.model.BrigadeBodyV2;
import org.igeolab.iot.pt.server.api.model.BrigadeEvent;
import org.igeolab.iot.pt.server.api.model.BrigadePayload;
import org.igeolab.iot.pt.server.api.model.BrigadeResource;
import org.igeolab.iot.pt.server.api.model.BrigadeTrip;
import org.igeolab.iot.pt.server.api.model.CalendarItemId1;
import org.igeolab.iot.pt.server.api.model.CalendarSymbolId1;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.TripId2;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceQueryService;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeRepository;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.core.brigade.BrigadeTripRepository;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BrigadeGroupDetailsService {

    private final AgencyService agencyService;
    private final BrigadeRepository brigadeRepository;
    private final BrigadeTripRepository brigadeTripRepository;
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
        var eventsByResourceId = brigadeEventQueryService.findAllByResourceIds(
                        resourceEntities.stream().map(BrigadeResourceEntity::getBrigadeResourceId).toList())
                .stream()
                .collect(Collectors.groupingBy(event -> event.getResource().getBrigadeResourceId()));

        var brigadeResources = resourceEntities.stream()
                .map(resource -> map(resource, eventsByResourceId.getOrDefault(resource.getBrigadeResourceId(), List.of())))
                .toList();

        return new BrigadeBodyV2()
                .brigadeName(brigadeGroup.getName())
                .calendarSymbolId(mapCalendarSymbolId(brigadeGroup))
                .brigadeResources(brigadeResources);
    }

    @PreAgencyAuthorize
    public BrigadeBody getBrigadeByBrigadeName(String instance, BrigadePayload brigadePayload) {
        var agencyEntity = this.agencyService.findAgencyByAgencyCode(instance);
        List<BrigadeTrip> trips = brigadeTripRepository.findAllByBrigadeName(instance, brigadePayload.getBrigadeName()).stream()
                .map(brigade -> new BrigadeTrip()
                        .tripId(new TripId2()
                                .routeId(new RouteId()
                                        .line(brigade.getLine())
                                        .name(brigade.getName()))
                                .variantName(brigade.getVariant())
                                .variantMode(TripVariantModeMapper.map(brigade.getMode())))
                        .tripSequence(brigade.getTripSequence())
                        .origin(brigade.getOrigin())
                        .destination(brigade.getDestination())
                        .travelTimeInSeconds(brigade.getTravelTimeInSeconds())
                        .arrivalTime(0)
                        .departureTime(brigade.getDepartureTimeInSeconds()))
                .toList();

        return brigadeRepository.findBrigadeEntitiesByAgencyAndBrigadeNumber(agencyEntity, brigadePayload.getBrigadeName())
                .map(brigadeEntity -> new BrigadeBody()
                        .brigadeName(brigadeEntity.getBrigadeNumber())
                        .calendarSymbolId(new CalendarSymbolId1()
                                .calendarItemId(new CalendarItemId1()
                                        .code(brigadeEntity.getCalendar().getCalendarItem().getSequenceHex()))
                                .symbol(brigadeEntity.getCalendar().getDesignation()))
                        .trips(trips))
                .orElse(null);
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
