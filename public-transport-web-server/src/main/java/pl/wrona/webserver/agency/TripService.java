package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.util.SloppyMath;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.igeolab.iot.pt.server.api.model.Trips;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.agency.entity.Route;
import pl.wrona.webserver.agency.entity.Stop;
import pl.wrona.webserver.agency.entity.StopTimeEntity;
import pl.wrona.webserver.agency.entity.StopTimeId;
import pl.wrona.webserver.agency.entity.TripEntity;
import pl.wrona.webserver.security.AppUser;

import java.time.LocalTime;
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
    public Status createTrip(Trip trips) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        List<String> stopIds = trips.getStops().stream()
                .map(StopTime::getStopId)
                .toList();

        Map<String, Stop> stopDictionary = stopService.mapStopByIdsIn(stopIds);
        Route route = routeQueryService.findRouteByNameAndLine(trips.getName(), trips.getLine());

        TripEntity lastTrip = tripRepository.findFirstByRouteOrderByVariant(route);

        TripEntity trip = new TripEntity();
        trip.setRoute(route);
        trip.setVariant(trip.getVariant());
        trip.setHeadsign(trips.getHeadsign());

        TripEntity savedTrip = tripRepository.save(trip);

        StopTime[] stopTimes = trips.getStops().toArray(StopTime[]::new);

        List<StopTimeEntity> entities = IntStream.range(0, stopTimes.length)
                .mapToObj(i -> {
                    StopTime stopTime = stopTimes[i];

                    StopTimeEntity entity = new StopTimeEntity();

                    StopTimeId stopTimeId = new StopTimeId();
                    stopTimeId.setTripId(savedTrip.getTripId());
                    stopTimeId.setStopSequence(i + 1);
                    entity.setStopTimeId(stopTimeId);

                    entity.setStop(stopDictionary.get(stopTime.getStopId()));
                    entity.setArrivalTime(LocalTime.MIDNIGHT.plusMinutes(stopTime.getMinutes().longValue()));
                    entity.setDepartureTime(LocalTime.MIDNIGHT.plusMinutes(stopTime.getMinutes().longValue()));

                    return entity;
                }).toList();

        stopTimeRepository.saveAll(entities);

        return new Status()
                .status("CREATED");
    }

    public Trips getTrips(RouteId routeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        Route route = routeQueryService.findRouteByNameAndLine(routeId.getName(), routeId.getLine());
        List<TripEntity> trips = tripRepository.findAllByRoute(route);
        List<Trip> tripsResponse = trips.stream()
                .map(trip -> new Trip()
                        .name(route.getName())
                        .line(route.getLine())
                        .variant(trip.getVariant())
                        .headsign(trip.getHeadsign())).toList();


        return new Trips()
                .trips(tripsResponse);
    }

    public Trips getTripByVariant(TripId tripId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        Route route = routeQueryService.findRouteByNameAndLine(tripId.getName(), tripId.getLine());
        TripEntity tripEntity = tripRepository.findAllByRouteAndVariant(route, tripId.getVariant());
        Trip tripsResponse = Optional.of(tripEntity)
                .map(trip -> new Trip()
                        .name(route.getName())
                        .line(route.getLine())
                        .variant(trip.getVariant())
                        .headsign(trip.getHeadsign()))
                .orElse(null);

        List<StopTimeEntity> stopTimes = stopTimeRepository.findAllByTripId(tripEntity.getTripId());

        stopTimes.forEach((StopTimeEntity stopTime) -> {
            tripsResponse.addStopsItem(new StopTime()
                    .stopId(stopTime.getStop().getStopId())
                    .stopName(stopTime.getStop().getName())
                    .lon((float) stopTime.getStop().getLon())
                    .lat((float) stopTime.getStop().getLat()));
        });
        return new Trips()
                .trips(List.of(tripsResponse));
    }

    public Trip measureDistance(Trip trips) {
        List<StopTime> stopTimes = pairConsecutiveElements(trips.getStops()).stream()
                .map(pairStopTime -> {
                    if (pairStopTime.getLeft() == null) {
                        return new StopTime()
                                .stopId(pairStopTime.getRight().getStopId())
                                .stopName(pairStopTime.getRight().getStopName())
                                .lon(pairStopTime.getRight().getLon())
                                .lat(pairStopTime.getRight().getLat())
                                .meters(0.0f)
                                .minutes(0.0f);
                    }

                    float haversinMeters = (float) SloppyMath.haversinMeters(pairStopTime.getLeft().getLat(),
                            pairStopTime.getLeft().getLon(),
                            pairStopTime.getRight().getLat(),
                            pairStopTime.getRight().getLon());

                    return new StopTime()
                            .stopId(pairStopTime.getRight().getStopId())
                            .stopName(pairStopTime.getRight().getStopName())
                            .lon(pairStopTime.getRight().getLon())
                            .lat(pairStopTime.getRight().getLat())
                            .meters(haversinMeters)
                            .minutes((float)(haversinMeters * 0.2777));
                })
                .toList();

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
