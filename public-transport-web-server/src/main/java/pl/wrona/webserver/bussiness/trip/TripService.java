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
    public Status updateTrip(UpdateTripDetailsRequest updateTripDetailsRequest) {
        var tripId = updateTripDetailsRequest.getTripId();
        var trip = updateTripDetailsRequest.getTrip().getTrip();

        List<Long> stopIds = trip.getStops().stream()
                .map(StopTime::getStopId)
                .toList();

        Map<Long, StopEntity> stopDictionary = stopService.mapStopByIdsIn(stopIds);
        var route = routeQueryService.findRouteByNameAndLine(tripId.getName(), tripId.getLine());
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

    public TripsDetails getTripByVariant(TripId tripId) {
        var route = routeQueryService.findRouteByNameAndLine(tripId.getName(), tripId.getLine());
        var tripEntity = tripRepository.findAllByRouteAndVariantNameAndMode(route, tripId.getVariant(), TripModeMapper.map(tripId.getMode()));

        var tripResponse = Optional.ofNullable(tripEntity)
                .map(trip -> TripMapper.map(trip, Map.of()))
                .orElse(null);

        List<StopTimeEntity> stopTimes = tripEntity != null
                ? stopTimeRepository.findAllByTripId(tripEntity.getTripId()) : List.of();

        stopTimes.forEach((StopTimeEntity stopTime) -> {
            tripResponse.addStopsItem(new StopTime()
                    .stopId(stopTime.getStopEntity().getStopId())
                    .stopName(stopTime.getStopEntity().getName())
                    .calculatedSeconds(stopTime.getCalculatedTimeSeconds())
                    .customizedSeconds(stopTime.getCustomizedTimeSeconds())
                    .meters(stopTime.getDistanceMeters())
                    .bdot10k(stopTime.getStopEntity().isBdot10k())
                    .lon((float) stopTime.getStopEntity().getLon())
                    .lat((float) stopTime.getStopEntity().getLat()));
        });

        if (tripEntity.getGeometry() != null) {
            try {
                List<Point2D> geometry = objectMapper.readValue(tripEntity.getGeometry(), new TypeReference<List<List<Float>>>() {
                        }).stream()
                        .map((List<Float> point) -> new Point2D()
                                .lat(point.get(0))
                                .lon(point.get(1)))
                        .toList();

                tripResponse.setGeometry(geometry);
            } catch (Exception exception) {
            }
        }

        var stopsIds = List.of(route.getOriginStopId(), route.getDestinationStopId());
        return new TripsDetails()
                .route(RouteMapper.map(route, stopService.mapStopByIdsIn(stopsIds), Map.of()))
                .trip(tripResponse);
    }

    public TripEntity findByTripId(TripId tripId) {
        return tripRepository.findByLineAndNameAndVariantAndMode(tripId.getLine(), tripId.getName(), tripId.getVariant(), TripModeMapper.map(tripId.getMode()));
    }
}
