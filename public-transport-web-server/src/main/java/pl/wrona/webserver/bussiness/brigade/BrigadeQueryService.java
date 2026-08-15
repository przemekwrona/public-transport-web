package pl.wrona.webserver.bussiness.brigade;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeDeleteBody;
import org.igeolab.iot.pt.server.api.model.BrigadePatchBody;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.bussiness.trip.TripService;
import pl.wrona.webserver.core.brigade.BrigadeEntity;
import pl.wrona.webserver.core.brigade.BrigadeRepository;
import pl.wrona.webserver.core.brigade.BrigadeTripEntity;
import pl.wrona.webserver.core.brigade.BrigadeTripRepository;
import pl.wrona.webserver.bussiness.calendar.CalendarSymbolQueryService;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.time.LocalTime;

@Service
@AllArgsConstructor
public class BrigadeQueryService {

    private final AgencyService agencyService;

    private final BrigadeRepository brigadeRepository;
    private final BrigadeTripRepository brigadeTripRepository;
    private final CalendarSymbolQueryService calendarSymbolQueryService;

    private final TripService tripService;

    @PreAgencyAuthorize
    @Transactional
    public Status updateBrigade(String instance, BrigadePatchBody brigadePatchBody) {
        String brigadeId = brigadePatchBody.getBrigadePayload().getBrigadeName();
        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);
        var calendarSymbolEntity = calendarSymbolQueryService.findByAgencyAndBrigadeAndCalendarAndSymbol(instance, "", brigadePatchBody.getBrigadeBody().getCalendarSymbolId().getCalendarItemId().getCode(), brigadePatchBody.getBrigadeBody().getCalendarSymbolId().getSymbol());

        brigadeRepository.findBrigadeEntitiesByAgencyAndBrigadeNumber(agencyEntity, brigadeId).ifPresent((BrigadeEntity entity) -> {
            entity.setBrigadeNumber(brigadePatchBody.getBrigadeBody().getBrigadeName());
            entity.setCalendar(calendarSymbolEntity);

            brigadeRepository.save(entity);

            brigadeTripRepository.deleteAllByBrigade(entity);

            var brigadeTrips = brigadePatchBody.getBrigadeBody().getTrips().stream()
                    .map(brigadeTrip -> {
                        var brigadeTripEntity = new BrigadeTripEntity();

                        brigadeTripEntity.setLine(brigadeTrip.getTripId().getRouteId().getLine());
                        brigadeTripEntity.setName(brigadeTrip.getTripId().getRouteId().getName());
                        brigadeTripEntity.setVariant(brigadeTrip.getTripId().getVariantName());
                        brigadeTripEntity.setMode(TripVariantModeMapper.map(brigadeTrip.getTripId().getVariantMode()));
                        brigadeTripEntity.setTripSequence(brigadeTrip.getTripSequence());
                        brigadeTripEntity.setBrigadeTripId(brigadeTripEntity.stringifyId(agencyEntity, entity));

                        brigadeTripEntity.setBrigade(entity);
                        brigadeTripEntity.setOrigin(brigadeTrip.getOrigin());
                        brigadeTripEntity.setDestination(brigadeTrip.getDestination());
                        brigadeTripEntity.setTravelTimeInSeconds(brigadeTrip.getTravelTimeInSeconds());

                        int departureTime = LocalTime.MIN.plusSeconds(brigadeTrip.getDepartureTime()).toSecondOfDay();
                        brigadeTripEntity.setDepartureTimeInSeconds(departureTime);

                        int arrivalTime = LocalTime.MIN.plusSeconds(brigadeTrip.getArrivalTime()).toSecondOfDay();
//                        brigadeTripEntity.set(arrivalTime);

                        var tripId = new TripId()
                                .routeId(new RouteId()
                                        .line(brigadeTrip.getTripId().getRouteId().getLine())
                                        .name(brigadeTrip.getTripId().getRouteId().getName()))
                                .variantName(brigadeTrip.getTripId().getVariantName())
                                .variantMode(brigadeTrip.getTripId().getVariantMode());

                        var tripEntity = tripService.findByTripId(tripId);
                        brigadeTripEntity.setRootTrip(tripEntity);

//                        brigadeTripEntity.setVariantDesignation(tripEntity.getVariantDesignation());
//                        brigadeTripEntity.setVariantDescription(tripEntity.getVariantDescription());

                        return brigadeTripEntity;
                    }).toList();

            brigadeTripRepository.saveAll(brigadeTrips);
        });

        return new Status().status(Status.StatusEnum.SUCCESS);
    }

    @PreAgencyAuthorize
    public Status deleteBrigade(String instance, BrigadeDeleteBody brigadeDeleteBody) {
        brigadeRepository.findBrigadeEntitiesByAgencyAndBrigadeNumber(agencyService.getLoggedAgency(), brigadeDeleteBody.getBrigadeName()).ifPresent(brigadeRepository::delete);
        return new Status().status(Status.StatusEnum.DELETED);
    }
}
