package pl.wrona.webserver.bussiness.trip;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Point2D;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.igeolab.iot.pt.server.api.model.TripsDetails;
import org.igeolab.iot.pt.server.api.model.UpdateTripDetailsRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.mapper.RouteMapper;
import pl.wrona.webserver.core.mapper.TripMapper;
import pl.wrona.webserver.core.mapper.TripModeMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final RouteQueryService routeQueryService;
    private final StopService stopService;
    private final StopTimeRepository stopTimeRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @PreAgencyAuthorize
    public Status updateTrip(String instance, UpdateTripDetailsRequest updateTripDetailsRequest) {
        var tripId = updateTripDetailsRequest.getTripId();
        var trip = updateTripDetailsRequest.getBody().getItem();

        List<Long> stopIds = trip.getStops().stream()
                .map(StopTime::getStopId)
                .toList();

        Map<Long, StopEntity> stopDictionary = stopService.mapStopByIdsIn(stopIds);
        var route = routeQueryService.findRouteByAgencyCodeAndRouteId(instance, tripId.getRouteId());
        TripEntity tripEntity = tripRepository.findAllByRouteAndVariantNameAndMode(route, tripId.getVariant(), TripModeMapper.map(tripId.getMode()));
        TripEntity updatedTrip = TripMapper.update(tripEntity, trip);
        Optional<StopTime> lastStopOptional = trip.getStops().stream().reduce((first, second) -> second);
        lastStopOptional.ifPresent(lastStop -> {
//            updatedTrip.setDistanceInMeters(lastStop.getMeters());
            updatedTrip.setTravelTimeInSeconds(lastStop.getCalculatedSeconds());
        });


        stopTimeRepository.deleteByTripId(updatedTrip.getTripId());

        StopTime[] stopTimes = trip.getStops().toArray(StopTime[]::new);

        List<StopTimeEntity> entities = IntStream.range(0, stopTimes.length)
                .mapToObj(i -> {
                    StopTime stopTime = stopTimes[i];

                    StopTimeEntity entity = new StopTimeEntity();

                    StopTimeId stopTimeId = new StopTimeId();
                    stopTimeId.setTripId(updatedTrip.getTripId());
                    stopTimeId.setStopSequence(i + 1);
                    entity.setStopTimeId(stopTimeId);

                    entity.setStopEntity(stopDictionary.get(stopTime.getStopId()));
                    entity.setDistanceMeters(stopTime.getMeters());
                    entity.setCalculatedTimeSeconds(stopTime.getCalculatedSeconds());

                    return entity;
                }).toList();

        stopTimeRepository.saveAll(entities);
        tripRepository.save(updatedTrip);

        return new Status()
                .status(Status.StatusEnum.SUCCESS);
    }

    public TripEntity findByTripId(TripId tripId) {
        return tripRepository.findByLineAndNameAndVariantAndMode(tripId.getRouteId().getLine(), tripId.getRouteId().getName(), tripId.getVariant(), TripModeMapper.map(tripId.getMode()));
    }
}
