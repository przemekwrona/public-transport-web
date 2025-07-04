package pl.wrona.webserver.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CreateTripDetailsRequest;
import org.igeolab.iot.pt.server.api.model.Point2D;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.igeolab.iot.pt.server.api.model.TripsDetails;
import org.igeolab.iot.pt.server.api.model.UpdateTripDetailsRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.agency.RouteQueryService;
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
    public Status createTrip(CreateTripDetailsRequest createTripDetailsRequest) {
        Trip trip = createTripDetailsRequest.getTrip().getTrip();
        List<Long> stopIds = trip.getStops().stream()
                .map(StopTime::getStopId)
                .toList();

        Map<Long, StopEntity> stopDictionary = stopService.mapStopByIdsIn(stopIds);
        var route = routeQueryService.findRouteByNameAndLine(trip.getName(), trip.getLine());

        TripEntity tripEntity = TripMapper.map(trip);
        tripEntity.setRoute(route);
        var lastStop = trip.getStops().stream()
                .reduce((first, second) -> second);


        tripEntity.setDistanceInMeters(lastStop.map(StopTime::getMeters).orElse(0));
        tripEntity.setTravelTimeInSeconds(lastStop.map(StopTime::getSeconds).orElse(0));

        TripEntity savedTrip = tripRepository.save(tripEntity);

        StopTime[] stopTimes = trip.getStops().toArray(StopTime[]::new);

        List<StopTimeEntity> entities = IntStream.range(0, stopTimes.length)
                .mapToObj(i -> {
                    StopTime stopTime = stopTimes[i];

                    StopTimeEntity entity = new StopTimeEntity();

                    StopTimeId stopTimeId = new StopTimeId();
                    stopTimeId.setTripId(savedTrip.getTripId());
                    stopTimeId.setStopSequence(i + 1);
                    entity.setStopTimeId(stopTimeId);

                    entity.setStopEntity(stopDictionary.get(stopTime.getStopId()));
                    entity.setArrivalSecond(stopTime.getSeconds());
                    entity.setDepartureSecond(stopTime.getSeconds());
                    entity.setDistanceMeters(stopTime.getMeters());

                    return entity;
                }).toList();

        stopTimeRepository.saveAll(entities);

        return new Status()
                .status(Status.StatusEnum.CREATED);
    }

    @Transactional
    public Status deleteTripByTripId(TripId tripId) {
        TripEntity deleteTrip = tripRepository.findByLineAndNameAndVariantAndMode(tripId.getLine(), tripId.getName(), tripId.getVariant(), TripModeMapper.map(tripId.getMode()));
        tripRepository.delete(deleteTrip);
        return new Status().status(Status.StatusEnum.DELETED);
    }

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
            updatedTrip.setDistanceInMeters(lastStop.getMeters());
            updatedTrip.setTravelTimeInSeconds(lastStop.getSeconds());
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
                    entity.setArrivalSecond(stopTime.getSeconds());
                    entity.setDepartureSecond(stopTime.getSeconds());
                    entity.setDistanceMeters(stopTime.getMeters());

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
                .map(trip -> TripMapper.map(trip))
                .orElse(null);

        List<StopTimeEntity> stopTimes = tripEntity != null
                ? stopTimeRepository.findAllByTripId(tripEntity.getTripId()) : List.of();

        stopTimes.forEach((StopTimeEntity stopTime) -> {
            tripResponse.addStopsItem(new StopTime()
                    .stopId(stopTime.getStopEntity().getStopId())
                    .stopName(stopTime.getStopEntity().getName())
                    .arrivalTime(stopTime.getArrivalSecond())
                    .departureTime(stopTime.getDepartureSecond())
                    .seconds(stopTime.getDepartureSecond())
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
                .route(RouteMapper.map(route, stopService.mapStopByIdsIn(stopsIds)))
                .trip(tripResponse);
    }


    public TripEntity findByTripId(TripId tripId) {
        return tripRepository.findByLineAndNameAndVariantAndMode(tripId.getLine(), tripId.getName(), tripId.getVariant(), TripModeMapper.map(tripId.getMode()));
    }
}
