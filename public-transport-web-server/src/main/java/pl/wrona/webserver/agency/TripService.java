package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.util.SloppyMath;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.igeolab.iot.pt.server.api.model.TripMode;
import org.igeolab.iot.pt.server.api.model.Trips;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.agency.entity.Stop;
import pl.wrona.webserver.agency.entity.StopTimeEntity;
import pl.wrona.webserver.agency.entity.StopTimeId;
import pl.wrona.webserver.agency.entity.TripEntity;
import pl.wrona.webserver.agency.entity.TripVariantMode;
import pl.wrona.webserver.agency.mapper.RouteMapper;

import java.util.ArrayList;
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

    @Transactional
    public Status createTrip(Trip trip) {
        List<String> stopIds = trip.getStops().stream()
                .map(StopTime::getStopId)
                .toList();

        Map<String, Stop> stopDictionary = stopService.mapStopByIdsIn(stopIds);
        var route = routeQueryService.findRouteByNameAndLine(trip.getName(), trip.getLine());

        TripEntity tripEntity = new TripEntity();
        tripEntity.setRoute(route);
        tripEntity.setVariant(trip.getVariant());

        if (TripMode.MAIN.equals(trip.getMode())) {
            tripEntity.setMode(TripVariantMode.MAIN);
        } else if (TripMode.BACK.equals(trip.getMode())) {
            tripEntity.setMode(TripVariantMode.BACK);
        }
        tripEntity.setHeadsign(trip.getHeadsign());
        tripEntity.setCommunicationVelocity(trip.getCommunicationVelocity());

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

                    entity.setStop(stopDictionary.get(stopTime.getStopId()));
                    entity.setArrivalSecond(stopTime.getSeconds());
                    entity.setDepartureSecond(stopTime.getSeconds());
                    entity.setDistanceMeters(stopTime.getMeters());

                    return entity;
                }).toList();

        stopTimeRepository.saveAll(entities);

        return new Status()
                .status("CREATED");
    }

    @Transactional
    public Status updateTrip(Trip trip) {
        List<String> stopIds = trip.getStops().stream()
                .map(StopTime::getStopId)
                .toList();

        Map<String, Stop> stopDictionary = stopService.mapStopByIdsIn(stopIds);
        var route = routeQueryService.findRouteByNameAndLine(trip.getName(), trip.getLine());
        TripEntity tripEntity = tripRepository.findAllByRouteAndVariantAndMode(route, trip.getVariant(), TripModeMapper.map(trip.getMode()));
        tripEntity.setHeadsign(trip.getHeadsign());
        tripEntity.setCommunicationVelocity(trip.getCommunicationVelocity());
        stopTimeRepository.deleteByTripId(tripEntity.getTripId());

        StopTime[] stopTimes = trip.getStops().toArray(StopTime[]::new);

        List<StopTimeEntity> entities = IntStream.range(0, stopTimes.length)
                .mapToObj(i -> {
                    StopTime stopTime = stopTimes[i];

                    StopTimeEntity entity = new StopTimeEntity();

                    StopTimeId stopTimeId = new StopTimeId();
                    stopTimeId.setTripId(tripEntity.getTripId());
                    stopTimeId.setStopSequence(i + 1);
                    entity.setStopTimeId(stopTimeId);

                    entity.setStop(stopDictionary.get(stopTime.getStopId()));
                    entity.setArrivalSecond(stopTime.getSeconds());
                    entity.setDepartureSecond(stopTime.getSeconds());
                    entity.setDistanceMeters(stopTime.getMeters());

                    return entity;
                }).toList();

        stopTimeRepository.saveAll(entities);

        return new Status()
                .status("UPDATED");
    }

    public Trips getTrips(RouteId routeId) {
        var route = routeQueryService.findRouteByNameAndLine(routeId.getName(), routeId.getLine());
        List<TripEntity> trips = tripRepository.findAllByRoute(route);
        List<Trip> tripsResponse = trips.stream()
                .map(trip -> new Trip()
                        .name(route.getName())
                        .line(route.getLine())
                        .variant(trip.getVariant())
                        .mode(TripModeMapper.map(trip.getMode()))
                        .headsign(trip.getHeadsign())).toList();

        return new Trips()
                .route(RouteMapper.map(route))
                .trips(tripsResponse);
    }

    public Trips getTripByVariant(TripId tripId) {
        var route = routeQueryService.findRouteByNameAndLine(tripId.getName(), tripId.getLine());
        TripEntity tripEntity = tripRepository.findAllByRouteAndVariantAndMode(route, tripId.getVariant(), TripVariantMode.MAIN);

        Trip tripsResponse = Optional.ofNullable(tripEntity)
                .map(trip -> new Trip()
                        .name(route.getName())
                        .line(route.getLine())
                        .variant(trip.getVariant())
                        .headsign(trip.getHeadsign()))
                .orElse(null);

        List<StopTimeEntity> stopTimes = tripEntity != null
                ? stopTimeRepository.findAllByTripId(tripEntity.getTripId()) : List.of();

        stopTimes.forEach((StopTimeEntity stopTime) -> {
            tripsResponse.addStopsItem(new StopTime()
                    .stopId(stopTime.getStop().getStopId())
                    .stopName(stopTime.getStop().getName())
                    .arrivalTime(stopTime.getArrivalSecond())
                    .departureTime(stopTime.getDepartureSecond())
                    .seconds(stopTime.getDepartureSecond())
                    .meters(stopTime.getDistanceMeters())
                    .lon((float) stopTime.getStop().getLon())
                    .lat((float) stopTime.getStop().getLat()));
        });
        return new Trips()
                .trips(List.of(tripsResponse));
    }

    public Trip measureDistance(Trip trips) {
        int meters = 0;
        int seconds = 0;

        List<StopTime> stopTimes = new ArrayList<>();

        for (Pair<StopTime, StopTime> pairStopTime : pairConsecutiveElements(trips.getStops())) {
            if (pairStopTime.getLeft() == null) {
                stopTimes.add(new StopTime()
                        .stopId(pairStopTime.getRight().getStopId())
                        .stopName(pairStopTime.getRight().getStopName())
                        .lon(pairStopTime.getRight().getLon())
                        .lat(pairStopTime.getRight().getLat())
                        .meters(0)
                        .seconds(0));
            } else {
                int haversinMeters = (int) SloppyMath.haversinMeters(pairStopTime.getLeft().getLat(),
                        pairStopTime.getLeft().getLon(),
                        pairStopTime.getRight().getLat(),
                        pairStopTime.getRight().getLon());

                meters = meters + haversinMeters;

                // Communication speed 45km/h
                int velocityKmPerH = Optional.ofNullable(trips.getCommunicationVelocity()).orElse(45);
                double velocityMetersPerSec = (velocityKmPerH * 1000.0) / 3600.0d;
                seconds = seconds + (int) (((double) haversinMeters) / velocityMetersPerSec);

                stopTimes.add(new StopTime()
                        .stopId(pairStopTime.getRight().getStopId())
                        .stopName(pairStopTime.getRight().getStopName())
                        .lon(pairStopTime.getRight().getLon())
                        .lat(pairStopTime.getRight().getLat())
                        .meters(meters)
                        .arrivalTime(seconds)
                        .departureTime(seconds)
                        .seconds(seconds));
            }

        }

        return new Trip()
                .line(trips.getLine())
                .variant(trips.getVariant())
                .headsign(trips.getHeadsign())
                .stops(stopTimes);
    }

    public static List<Pair<StopTime, StopTime>> pairConsecutiveElements(List<StopTime> elements) {
        List<Pair<StopTime, StopTime>> pairedElements = new ArrayList<>();
        pairedElements.add(Pair.of(null, elements.get(0)));

        for (int i = 0; i < elements.size() - 1; i++) {
            pairedElements.add(Pair.of(elements.get(i), elements.get(i + 1)));
        }
        return pairedElements;
    }

}
