package pl.wrona.webserver.bussiness.brigade;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeBody;
import org.igeolab.iot.pt.server.api.model.BrigadeDeleteBody;
import org.igeolab.iot.pt.server.api.model.BrigadePatchBody;
import org.igeolab.iot.pt.server.api.model.BrigadePayload;
import org.igeolab.iot.pt.server.api.model.BrigadeTrip;
import org.igeolab.iot.pt.server.api.model.CalendarItemId1;
import org.igeolab.iot.pt.server.api.model.CalendarSymbolId1;
import org.igeolab.iot.pt.server.api.model.GetBrigadeBody;
import org.igeolab.iot.pt.server.api.model.GetBrigadeResponse;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.igeolab.iot.pt.server.api.model.TripId2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.bussiness.trip.TripService;
import pl.wrona.webserver.core.brigade.BrigadeEntity;
import pl.wrona.webserver.core.brigade.BrigadeRepository;
import pl.wrona.webserver.core.brigade.BrigadeTripEntity;
import pl.wrona.webserver.core.brigade.BrigadeTripRepository;
import pl.wrona.webserver.core.calendar.CalendarQueryService;
import pl.wrona.webserver.bussiness.calendar.CalendarSymbolQueryService;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.time.LocalTime;
import java.util.List;

@Service
@AllArgsConstructor
public class BrigadeQueryService {

    private final AgencyService agencyService;

    private final BrigadeRepository brigadeRepository;
    private final BrigadeTripRepository brigadeTripRepository;
    private final CalendarSymbolQueryService calendarSymbolQueryService;
    private final CalendarQueryService calendarQueryService;

    private final TripService tripService;

    @PreAgencyAuthorize
    @Transactional
    public Status createBrigade(String instance, BrigadeBody request) {

        if (existsByBrigadeName(request.getBrigadeName())) {
            throw new BusinessException("1000", "Brigade with name %s already exists. Select another one.".formatted(request.getBrigadeName()));
        }

        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);
        var calendarSymbolEntity = calendarSymbolQueryService.findByAgencyAndCalendarAndSymbol(instance, request.getCalendarSymbolId().getCalendarItemId().getCode(), request.getCalendarSymbolId().getSymbol());

        var brigadeEntity = new BrigadeEntity();
        brigadeEntity.setBrigadeNumber(request.getBrigadeName());
        brigadeEntity.setCalendar(calendarSymbolEntity);

        brigadeEntity.setAgency(agencyEntity);

        var savedBrigade = brigadeRepository.save(brigadeEntity);

        var brigadeTrips = request.getTrips().stream()
                .map(brigadeTrip -> {
                    var brigadeTripEntity = new BrigadeTripEntity();

                    brigadeTripEntity.setLine(brigadeTrip.getTripId().getRouteId().getLine());
                    brigadeTripEntity.setName(brigadeTrip.getTripId().getRouteId().getName());
                    brigadeTripEntity.setVariant(brigadeTrip.getTripId().getVariantName());
                    brigadeTripEntity.setMode(TripVariantModeMapper.map(brigadeTrip.getTripId().getVariantMode()));
                    brigadeTripEntity.setTripSequence(brigadeTrip.getTripSequence());
                    brigadeTripEntity.setBrigadeTripId(brigadeTripEntity.stringifyId(agencyEntity, savedBrigade));

                    brigadeTripEntity.setBrigade(savedBrigade);
                    brigadeTripEntity.setOrigin(brigadeTrip.getOrigin());
                    brigadeTripEntity.setDestination(brigadeTrip.getDestination());
                    brigadeTripEntity.setTravelTimeInSeconds(brigadeTrip.getTravelTimeInSeconds());

                    int secondOfDay = LocalTime.MIN.plusSeconds(brigadeTrip.getDepartureTime()).toSecondOfDay();
                    brigadeTripEntity.setDepartureTimeInSeconds(secondOfDay);

                    var tripId = new TripId()
                            .routeId(new RouteId()
                                    .line(brigadeTrip.getTripId().getRouteId().getLine())
                                    .name(brigadeTrip.getTripId().getRouteId().getName()))
                            .variantName(brigadeTrip.getTripId().getVariantName())
                            .variantMode(brigadeTrip.getTripId().getVariantMode());

                    var tripEntity = tripService.findByTripId(tripId);

                    brigadeTripEntity.setRootTrip(tripEntity);
                    brigadeTripEntity.setVariantDesignation(tripEntity.getVariantDesignation());
                    brigadeTripEntity.setVariantDescription(tripEntity.getVariantDescription());

                    return brigadeTripEntity;
                }).toList();

        brigadeTripRepository.saveAll(brigadeTrips);

        return new Status().status(Status.StatusEnum.CREATED);
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

    @PreAgencyAuthorize
    public GetBrigadeResponse findBrigades(String instance) {
        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);

        var brigades = brigadeRepository.findAllByAgency(agencyEntity).stream()
                .map(brigadeEntity -> new GetBrigadeBody()
                        .brigadeName(brigadeEntity.getBrigadeNumber())
                        .calendarSymbolId(new CalendarSymbolId1()
                                .calendarItemId(new CalendarItemId1()
                                        .code(brigadeEntity.getCalendar().getCalendarItem().getSequenceHex()))
                                .symbol(brigadeEntity.getCalendar().getDesignation()))
                        .calendarDesignation(brigadeEntity.getCalendar().getDesignation())
                        .calendarDescription(brigadeEntity.getCalendar().getDescription()))
                .toList();

        return new GetBrigadeResponse()
                .brigades(brigades);
    }

    @PreAgencyAuthorize
    @Transactional
    public Status updateBrigade(String instance, BrigadePatchBody brigadePatchBody) {
        String brigadeId = brigadePatchBody.getBrigadePayload().getBrigadeName();
        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);
        var calendarSymbolEntity = calendarSymbolQueryService.findByAgencyAndCalendarAndSymbol(instance, brigadePatchBody.getBrigadeBody().getCalendarSymbolId().getCalendarItemId().getCode(), brigadePatchBody.getBrigadeBody().getCalendarSymbolId().getSymbol());

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

    public boolean existsByBrigadeName(String brigadeName) {
        return this.brigadeRepository.existsBrigadeEntitiesByAgencyAndBrigadeNumber(agencyService.getLoggedAgency(), brigadeName);
    }

    @PreAgencyAuthorize
    public Status deleteBrigade(String instance, BrigadeDeleteBody brigadeDeleteBody) {
        brigadeRepository.findBrigadeEntitiesByAgencyAndBrigadeNumber(agencyService.getLoggedAgency(), brigadeDeleteBody.getBrigadeName()).ifPresent(brigadeRepository::delete);
        return new Status().status(Status.StatusEnum.DELETED);
    }
}
