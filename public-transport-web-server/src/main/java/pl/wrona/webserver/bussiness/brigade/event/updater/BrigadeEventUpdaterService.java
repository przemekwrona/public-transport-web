package pl.wrona.webserver.bussiness.brigade.event.updater;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.PutBrigadeEventBody;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceQueryService;
import pl.wrona.webserver.bussiness.trip.TripProfileQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.brigade.BrigadeEventCommandRepository;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.mapper.TripTrafficModeMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class BrigadeEventUpdaterService {

    private final BrigadeEventCommandRepository brigadeEventCommandRepository;
    private final BrigadeEventQueryService brigadeEventQueryService;
    private final BrigadeResourceQueryService brigadeResourceQueryService;
    private final BrigadeGroupQueryService brigadeGroupQueryService;
    private final TripQueryService tripQueryService;
    private final TripProfileQueryService tripProfileQueryService;

    @PreAgencyAuthorize
    @Transactional
    public Status putBrigadeEvent(String instance, String brigadeCode, String calendarCode, String symbol, String resourceCode, PutBrigadeEventBody putBrigadeEventBody) {
        var group = brigadeGroupQueryService.findByBrigadeCode(instance, brigadeCode, calendarCode, symbol);
        var destinationResource = brigadeResourceQueryService.findByBrigadeGroupAndResourceCode(group, resourceCode);
        var profile = tripProfileQueryService.findByAgencyAndRouteCodeAndTripCodeAndTrafficMode(instance, putBrigadeEventBody.getTripId().getRouteId().getRouteCode(), putBrigadeEventBody.getTripId().getTripCode(), TripTrafficModeMapper.map(putBrigadeEventBody.getTripId().getTrafficMode()));

        var brigadeEvent = brigadeEventQueryService.findByBrigadeGroupAndEventCode(group, putBrigadeEventBody.getSequenceHex());
        if (brigadeEvent == null) {
            brigadeEvent = new BrigadeEventEntity();
            brigadeEvent.setEventSequence(putBrigadeEventBody.getSequence());
            brigadeEvent.setEventCode(putBrigadeEventBody.getSequenceHex());
        }

        brigadeEvent.setStartSecond(putBrigadeEventBody.getStartSecond());
        brigadeEvent.setEndSecond(putBrigadeEventBody.getEndSecond());
        brigadeEvent.setResource(destinationResource);
        brigadeEvent.setTripProfile(profile);
        brigadeEvent.setLine(putBrigadeEventBody.getLine());
        brigadeEvent.setName(putBrigadeEventBody.getName());

        var savedEvent = brigadeEventCommandRepository.saveAndFlush(brigadeEvent);

        return new Status().status(Status.StatusEnum.SUCCESS);
    }

}
