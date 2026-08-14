package pl.wrona.webserver.bussiness.brigade.details;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeBody;
import org.igeolab.iot.pt.server.api.model.BrigadePayload;
import org.igeolab.iot.pt.server.api.model.BrigadeTrip;
import org.igeolab.iot.pt.server.api.model.CalendarItemId1;
import org.igeolab.iot.pt.server.api.model.CalendarSymbolId1;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.TripId2;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.brigade.BrigadeRepository;
import pl.wrona.webserver.core.brigade.BrigadeTripRepository;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;

@Service
@AllArgsConstructor
public class BrigadeGroupDetailsService {

    private final AgencyService agencyService;
    private final BrigadeRepository brigadeRepository;
    private final BrigadeTripRepository brigadeTripRepository;

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
}
