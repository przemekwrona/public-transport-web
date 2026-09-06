package pl.wrona.webserver.bussiness.brigade.optimizer;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.lucene.util.SloppyMath;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.bussiness.brigade.optimizer.OrToolsBrigadeOptimizer.TripRequest;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceQueryService;
import pl.wrona.webserver.bussiness.stop.FirstAndLastStop;
import pl.wrona.webserver.bussiness.stop.StopQueryService;
import pl.wrona.webserver.bussiness.trip.measure.TripDistanceMeasureService;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.brigade.BrigadeEventCommandRepository;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class OrToolsResourceOptimizerService {

    static final double DEADHEAD_SPEED_KMH = 30.0;

    private final BrigadeGroupQueryService brigadeGroupQueryService;
    private final BrigadeResourceQueryService brigadeResourceQueryService;
    private final BrigadeEventQueryService brigadeEventQueryService;
    private final StopQueryService stopQueryService;
    private final BrigadeEventCommandRepository brigadeEventCommandRepository;

    @PreAgencyAuthorize
    @Transactional
    public void optimizeBrigades(String instance, String brigadeCode, String calendarCode, String symbol) {
        var brigadeGroup = brigadeGroupQueryService.findByBrigadeCode(instance, brigadeCode, calendarCode, symbol);
        if (brigadeGroup == null) {
            return;
        }

        var resources = brigadeResourceQueryService.findByBrigadeGroup(brigadeGroup).stream()
                .sorted(Comparator.comparing(BrigadeResourceEntity::getResourceSequence, Comparator.nullsLast(Integer::compareTo)))
                .toList();
        var resourceIds = resources.stream()
                .map(BrigadeResourceEntity::getBrigadeResourceId)
                .toList();
        var events = brigadeEventQueryService.findAllByResourceIds(resourceIds);

        if (resources.isEmpty() || events.isEmpty()) {
            return;
        }

        var profiles = events.stream().map(BrigadeEventEntity::getTripProfile).toList();
        var edgeStopsByProfile = stopQueryService.findFirstAndLastStops(profiles);

        var problem = buildProblem(events, edgeStopsByProfile);
        if (problem.trips().isEmpty()) {
            return;
        }

        var tripsByVehicle = new OrToolsBrigadeOptimizer().assignTrips(problem.timeMatrix(), problem.trips(), resources.size());

        if (tripsByVehicle.isEmpty()) {
            return;
        }

        applyAssignment(resources, problem.events(), tripsByVehicle);
        brigadeEventCommandRepository.saveAll(problem.events());
    }

    static OptimizationProblem buildProblem(List<BrigadeEventEntity> events,
                                            Map<TripProfileEntity, FirstAndLastStop> edgeStopsByProfile) {
        List<double[]> coordinates = new ArrayList<>();
        Map<Long, Integer> stopIndexById = new HashMap<>();
        Map<Long, FirstAndLastStop> stopsByProfileId = indexStopsByProfileId(edgeStopsByProfile);
        List<TripRequest> trips = new ArrayList<>();
        List<BrigadeEventEntity> tripEvents = new ArrayList<>();

        for (var event : events) {
            if (event.getStartSecond() == null || event.getEndSecond() == null
                    || event.getStartSecond() > event.getEndSecond()) {
                continue;
            }
            var profile = event.getTripProfile();
            if (profile == null || profile.getTripProfileId() == null) {
                continue;
            }
            var edges = stopsByProfileId.get(profile.getTripProfileId());
            if (edges == null || edges.firstStop() == null || edges.lastStop() == null) {
                continue;
            }

            trips.add(new TripRequest(
                    locationIndex(edges.firstStop(), coordinates, stopIndexById),
                    locationIndex(edges.lastStop(), coordinates, stopIndexById),
                    event.getStartSecond(),
                    event.getEndSecond()));
            tripEvents.add(event);
        }

        var timeMAtrix = buildTimeMatrix(coordinates);

        return new OptimizationProblem(timeMAtrix, trips, tripEvents);
    }

    private static Map<Long, FirstAndLastStop> indexStopsByProfileId(Map<TripProfileEntity, FirstAndLastStop> edgeStopsByProfile) {
        Map<Long, FirstAndLastStop> stopsByProfileId = new HashMap<>();
        edgeStopsByProfile.forEach((profile, stops) -> {
            if (profile != null && profile.getTripProfileId() != null) {
                stopsByProfileId.putIfAbsent(profile.getTripProfileId(), stops);
            }
        });
        return stopsByProfileId;
    }

    private static int locationIndex(StopEntity stop, List<double[]> coordinates, Map<Long, Integer> stopIndexById) {
        if (stop.getStopId() == null) {
            coordinates.add(new double[]{stop.getLat(), stop.getLon()});
            return coordinates.size() - 1;
        }
        return stopIndexById.computeIfAbsent(stop.getStopId(), unused -> {
            coordinates.add(new double[]{stop.getLat(), stop.getLon()});
            return coordinates.size() - 1;
        });
    }

    private static long[][] buildTimeMatrix(List<double[]> coordinates) {
        int size = coordinates.size();
        long[][] timeMatrix = new long[size][size];
        for (int from = 0; from < size; from++) {
            for (int to = 0; to < size; to++) {
                timeMatrix[from][to] = travelSeconds(coordinates.get(from), coordinates.get(to));
            }
        }
        return timeMatrix;
    }

    private static long travelSeconds(double[] from, double[] to) {
        if (from == null || to == null || (from[0] == to[0] && from[1] == to[1])) {
            return 0L;
        }
        double meters = SloppyMath.haversinMeters(from[0], from[1], to[0], to[1])
                * TripDistanceMeasureService._10_PERCENT;
        return Math.max(0L, Math.round(TripDistanceMeasureService.convertToSeconds(meters, DEADHEAD_SPEED_KMH)));
    }

    static void applyAssignment(List<BrigadeResourceEntity> resources,
                                List<BrigadeEventEntity> tripEvents,
                                List<List<Integer>> tripsByVehicle) {
        int vehicleCount = Math.min(resources.size(), tripsByVehicle.size());
        for (int vehicle = 0; vehicle < vehicleCount; vehicle++) {
            var resource = resources.get(vehicle);
            for (int tripIndex : tripsByVehicle.get(vehicle)) {
                tripEvents.get(tripIndex).setResource(resource);
            }
        }
    }

    record OptimizationProblem(long[][] timeMatrix,
                               List<TripRequest> trips,
                               List<BrigadeEventEntity> events) {
    }
}
