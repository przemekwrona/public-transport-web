package pl.wrona.webserver.bussiness.trip.creator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CreateTripDetailsRequest;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.bussiness.trip.TripRepository;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.core.mapper.TripMapper;
import pl.wrona.webserver.core.mapper.TripModeMapper;
import pl.wrona.webserver.core.mapper.TripTrafficModeMapper;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class TripCreatorService {
    private final StopService stopService;
    private final TripRepository tripRepository;
    private final StopTimeRepository stopTimeRepository;
    private final TripQueryService tripQueryService;
    private final RouteQueryService routeQueryService;

    @Transactional
    @PreAgencyAuthorize
    public Status createTrip(String instance, CreateTripDetailsRequest createTripDetailsRequest) {
        boolean uniqueTripIndexExists = tripQueryService.existsUniqueTripIndex(instance,
                createTripDetailsRequest.getTrip().getRoute().getLine(),
                createTripDetailsRequest.getTrip().getRoute().getName(),
                TripModeMapper.map(createTripDetailsRequest.getTrip().getTrip().getMode()),
                TripTrafficModeMapper.map(createTripDetailsRequest.getTrip().getTrip().getTrafficMode()));

        if (uniqueTripIndexExists) {
            throw new BusinessException("ERROR:202510300047", "Trip index already exists");
        }

        Trip tripRequest = createTripDetailsRequest.getTrip().getTrip();
        List<Long> stopIds = tripRequest.getStops().stream()
                .map(StopTime::getStopId)
                .toList();

        Map<Long, StopEntity> stopDictionary = stopService.mapStopByIdsIn(stopIds);

        var route = routeQueryService.findRouteByAgencyCodeAndRouteId(instance, createTripDetailsRequest.getTripId().getRouteId());

        TripEntity tripEntity = TripMapper.map(tripRequest);
        tripEntity.setRoute(route);

        LocalDateTime now = LocalDateTime.now();
        tripEntity.setCreatedAt(now);
        tripEntity.setUpdatedAt(now);

        var lastStop = tripRequest.getStops().stream()
                .reduce((first, second) -> second);

        tripEntity.setDistanceInMeters(lastStop.map(StopTime::getMeters).orElse(0));
        tripEntity.setTravelTimeInSeconds(lastStop.map(StopTime::getCalculatedSeconds).orElse(0));

        TripEntity savedTrip = tripRepository.save(tripEntity);

        StopTime[] stopTimes = tripRequest.getStops().toArray(StopTime[]::new);

        List<StopTimeEntity> entities = IntStream.range(0, stopTimes.length)
                .mapToObj(i -> {
                    StopTime stopTime = stopTimes[i];

                    StopTimeEntity entity = new StopTimeEntity();

                    StopTimeId stopTimeId = new StopTimeId();
                    stopTimeId.setTripId(savedTrip.getTripId());
                    stopTimeId.setStopSequence(i + 1);
                    entity.setStopTimeId(stopTimeId);

                    entity.setStopEntity(stopDictionary.get(stopTime.getStopId()));
                    entity.setCalculatedTimeSeconds(stopTime.getCalculatedSeconds());
                    entity.setCustomizedTimeSeconds(stopTime.getCustomizedSeconds());
                    entity.setDistanceMeters(stopTime.getMeters());

                    return entity;
                }).toList();

        stopTimeRepository.saveAll(entities);

        return new Status()
                .status(Status.StatusEnum.CREATED);
    }

}
