package pl.wrona.webserver.agency.brigade;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeBody;
import org.igeolab.iot.pt.server.api.model.BrigadePatchBody;
import org.igeolab.iot.pt.server.api.model.BrigadePayload;
import org.igeolab.iot.pt.server.api.model.BrigadeTrip;
import org.igeolab.iot.pt.server.api.model.GetBrigadeBody;
import org.igeolab.iot.pt.server.api.model.GetBrigadeResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.agency.AgencyService;
import pl.wrona.webserver.agency.TripService;
import pl.wrona.webserver.agency.calendar.CalendarService;

import java.time.LocalTime;
import java.util.List;

@Service
@AllArgsConstructor
public class BrigadeService {

    private AgencyService agencyService;

    private final BrigadeRepository brigadeRepository;
    private final BrigadeTripRepository brigadeTripRepository;
    private final CalendarService calendarService;

    private final TripService tripService;

    @Transactional
    public Status createBrigade(BrigadeBody request) {
        var brigadeEntity = new BrigadeEntity();
        brigadeEntity.setBrigadeNumber(request.getBrigadeName());
        brigadeEntity.setCalendar(calendarService.findCalendarByCalendarName(request.getCalendarName()).orElse(null));

        brigadeEntity.setAgency(agencyService.getLoggedAgency());

        var savedBrigade = brigadeRepository.save(brigadeEntity);

        var brigadeTrips = request.getTrips().stream()
                .map(brigadeTrip -> {
                    var brigadeTripEntity = new BrigadeTripEntity();

                    brigadeTripEntity.setLine(brigadeTrip.getTripId().getLine());
                    brigadeTripEntity.setName(brigadeTrip.getTripId().getName());
                    brigadeTripEntity.setVariant(brigadeTrip.getTripId().getVariant());
                    brigadeTripEntity.setMode(brigadeTrip.getTripId().getMode());
                    brigadeTripEntity.setTripSequence(brigadeTrip.getTripSequence());
                    brigadeTripEntity.setBrigadeTripId(brigadeTripEntity.stringifyId(agencyService.getLoggedAgency(), brigadeTrip.getTripSequence()));

                    brigadeTripEntity.setBrigade(savedBrigade);
                    brigadeTripEntity.setOrigin(brigadeTrip.getOrigin());
                    brigadeTripEntity.setDestination(brigadeTrip.getDestination());
                    brigadeTripEntity.setTravelTimeInSeconds(brigadeTrip.getTravelTimeInSeconds());

                    int secondOfDay = LocalTime.MIN.plusSeconds(brigadeTrip.getArrivalTime()).toSecondOfDay();
                    brigadeTripEntity.setDepartureTimeInSeconds(secondOfDay);

                    var tripId = new TripId()
                            .line(brigadeTrip.getTripId().getLine())
                            .name(brigadeTrip.getTripId().getName())
                            .variant(brigadeTrip.getTripId().getVariant())
                            .mode(brigadeTrip.getTripId().getMode());

                    var tripEntity = tripService.findByTripId(tripId);

                    brigadeTripEntity.setRootTrip(tripEntity);
                    brigadeTripEntity.setVariantDesignation(tripEntity.getVariantDesignation());
                    brigadeTripEntity.setVariantDescription(tripEntity.getVariantDescription());

                    return brigadeTripEntity;
                }).toList();

        brigadeTripRepository.saveAll(brigadeTrips);

        return new Status().status(Status.StatusEnum.CREATED);
    }

    public BrigadeBody getBrigadeByBrigadeName(BrigadePayload brigadePayload) {
        List<BrigadeTrip> trips = brigadeTripRepository.findAllByBrigadeName(brigadePayload.getBrigadeName()).stream()
                .map(brigade -> new BrigadeTrip()
                        .tripId(new TripId()
                                .line(brigade.getLine())
                                .name(brigade.getName())
                                .variant(brigade.getVariant())
                                .mode(brigade.getMode()))
                        .tripSequence(brigade.getTripSequence())
                        .origin(brigade.getOrigin())
                        .destination(brigade.getDestination())
                        .travelTimeInSeconds(brigade.getTravelTimeInSeconds())
                        .arrivalTime(0)
                        .departureTime(brigade.getDepartureTimeInSeconds()))
                .toList();

        return brigadeRepository.findBrigadeEntitiesByBrigadeNumber(brigadePayload.getBrigadeName())
                .map(brigadeEntity -> new BrigadeBody()
                        .brigadeName(brigadeEntity.getBrigadeNumber())
                        .trips(trips))
                .orElse(null);
    }

    public GetBrigadeResponse findBrigades() {
        var brigades = brigadeRepository.findAll().stream()
                .map(brigadeEntity -> new GetBrigadeBody()
                        .brigadeName(brigadeEntity.getBrigadeNumber()))
                .toList();

        return new GetBrigadeResponse()
                .brigades(brigades);
    }

    @Transactional
    public Status updateBrigade(BrigadePatchBody brigadePatchBody) {
        String brigadeId = brigadePatchBody.getBrigadePayload().getBrigadeName();

        brigadeRepository.findBrigadeEntitiesByBrigadeNumber(brigadeId).ifPresent((BrigadeEntity entity) -> {
            entity.setBrigadeNumber(brigadePatchBody.getBrigadeBody().getBrigadeName());
            entity.setCalendar(calendarService.findCalendarByCalendarName(brigadePatchBody.getBrigadeBody().getCalendarName()).orElse(null));

            brigadeRepository.save(entity);

            brigadeTripRepository.deleteAllByBrigade(entity);

            var brigadeTrips = brigadePatchBody.getBrigadeBody().getTrips().stream()
                    .map(brigadeTrip -> {
                        var brigadeTripEntity = new BrigadeTripEntity();

                        brigadeTripEntity.setLine(brigadeTrip.getTripId().getLine());
                        brigadeTripEntity.setName(brigadeTrip.getTripId().getName());
                        brigadeTripEntity.setVariant(brigadeTrip.getTripId().getVariant());
                        brigadeTripEntity.setMode(brigadeTrip.getTripId().getMode());
                        brigadeTripEntity.setTripSequence(brigadeTrip.getTripSequence());
                        brigadeTripEntity.setBrigadeTripId(brigadeTripEntity.stringifyId(agencyService.getLoggedAgency(), brigadeTrip.getTripSequence()));

                        brigadeTripEntity.setBrigade(entity);
                        brigadeTripEntity.setOrigin(brigadeTrip.getOrigin());
                        brigadeTripEntity.setDestination(brigadeTrip.getDestination());
                        brigadeTripEntity.setTravelTimeInSeconds(brigadeTrip.getTravelTimeInSeconds());

                        int secondOfDay = LocalTime.MIN.plusSeconds(brigadeTrip.getArrivalTime()).toSecondOfDay();
                        brigadeTripEntity.setDepartureTimeInSeconds(secondOfDay);

                        var tripId = new TripId()
                                .line(brigadeTrip.getTripId().getLine())
                                .name(brigadeTrip.getTripId().getName())
                                .variant(brigadeTrip.getTripId().getVariant())
                                .mode(brigadeTrip.getTripId().getMode());

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
}
