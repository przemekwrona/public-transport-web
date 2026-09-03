package pl.wrona.webserver.bussiness.brigade.event.updater;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.PutBrigadeEventBody;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.bussiness.brigade.item.BrigadeItemQueryService;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceQueryService;
import pl.wrona.webserver.bussiness.trip.TripProfileQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.agency.TripTrafficMode;
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
    private final TripQueryService tripQueryService;
    private final TripProfileQueryService tripProfileQueryService;

    @PreAgencyAuthorize
    @Transactional
    public Status putBrigadeEvent(String instance, String brigadeCode, String calendarCode, String symbol, String resourceCode, PutBrigadeEventBody putBrigadeEventBody) {
        var resource = brigadeResourceQueryService.findByAgencyAndCalendarAndSymbolAndResourceCode(instance, brigadeCode, calendarCode, symbol, resourceCode);
        var profile = tripProfileQueryService.findByAgencyAndRouteCodeAndTripCodeAndTrafficMode(instance, putBrigadeEventBody.getTripId().getRouteId().getRouteCode(), putBrigadeEventBody.getTripId().getTripCode(), TripTrafficModeMapper.map(putBrigadeEventBody.getTripId().getTrafficMode()));

        var brigadeEvent = brigadeEventQueryService.findByAgencyAndCalendarAndSymbolAndEventHex(
                instance, calendarCode, symbol, putBrigadeEventBody.getSequenceHex());
        if (brigadeEvent == null) {
            brigadeEvent = new BrigadeEventEntity();
            brigadeEvent.setEventSequence(putBrigadeEventBody.getSequence());
            brigadeEvent.setEventCode(putBrigadeEventBody.getSequenceHex());
        }

        brigadeEvent.setStartSecond(putBrigadeEventBody.getStartSecond());
        brigadeEvent.setEndSecond(putBrigadeEventBody.getEndSecond());
        brigadeEvent.setResource(resource);
        brigadeEvent.setTripProfile(profile);
        brigadeEvent.setLine(putBrigadeEventBody.getLine());
        brigadeEvent.setName(putBrigadeEventBody.getName());

        brigadeEventCommandRepository.save(brigadeEvent);

        return new Status().status(Status.StatusEnum.SUCCESS);
    }

}
