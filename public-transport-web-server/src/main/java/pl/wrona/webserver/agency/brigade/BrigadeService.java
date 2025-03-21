package pl.wrona.webserver.agency.brigade;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeBody;
import org.igeolab.iot.pt.server.api.model.BrigadePayload;
import org.igeolab.iot.pt.server.api.model.GetBrigadeBody;
import org.igeolab.iot.pt.server.api.model.GetBrigadeResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.agency.AgencyService;
import pl.wrona.webserver.agency.TripService;

import java.time.LocalTime;

@Service
@AllArgsConstructor
public class BrigadeService {

    private AgencyService agencyService;

    private final BrigadeRepository brigadeRepository;
    private final BrigadeTripRepository brigadeTripRepository;

    private final TripService tripService;

    @Transactional
    public Status createBrigade(BrigadeBody request) {
        var brigadeEntity = new BrigadeEntity();
        brigadeEntity.setBrigadeNumber(request.getBrigadeNumber());
        brigadeEntity.setAgency(agencyService.getLoggedAgency());

        var savedBrigade = brigadeRepository.save(brigadeEntity);

        var brigadeTrips = request.getTrips().stream()
                .map(brigadeTrip -> {
                    var brigadeTripEntity = new BrigadeTripEntity();
                    brigadeTripEntity.setLine(brigadeTrip.getTripId().getLine());
                    brigadeTripEntity.setName(brigadeTrip.getTripId().getName());
                    brigadeTripEntity.setVariant(brigadeTrip.getTripId().getVariant());
                    brigadeTripEntity.setMode(brigadeTrip.getTripId().getMode());

                    brigadeTripEntity.setBrigade(savedBrigade);

                    int secondOfDay = LocalTime.parse(brigadeTrip.getArrivalTime()).toSecondOfDay();
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
        return brigadeRepository.findByBrigadeNumber(brigadePayload.getBrigadeNumber())
                .map(brigadeEntity -> new BrigadeBody()
                        .brigadeNumber(brigadeEntity.getBrigadeNumber()))
                .orElse(null);
    }

    public GetBrigadeResponse findBrigades() {
        var brigades = brigadeRepository.findAll().stream()
                .map(brigadeEntity -> new GetBrigadeBody()
                        .brigadeNumber(brigadeEntity.getBrigadeNumber()))
                .toList();

        return new GetBrigadeResponse()
                .brigades(brigades);
    }
}
