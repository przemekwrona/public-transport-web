package pl.wrona.webserver.bussiness.route.stop.sequencer;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.agency.TripTrafficMode;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.core.entity.StopEntity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StopGraphSequencerService {

    private static final Comparator<TripProfileEntity> PROFILE_COMPARATOR = Comparator
            .comparing(TripProfileEntity::isDefaultProfile, Comparator.reverseOrder())
            .thenComparing(StopGraphSequencerService::trafficModeWeight, Comparator.reverseOrder())
            .thenComparing(TripProfileEntity::getTripProfileId, Comparator.nullsLast(Long::compareTo));

    private final RouteQueryService routeQueryService;
    private final TripQueryService tripQueryService;
    private final StopTimeRepository stopTimeRepository;

    public StopGraph sequence(String agency, String routeCode, TripVariantMode mode) {
        RouteEntity route = routeQueryService.findRouteByAgencyCodeAndRouteCode(agency, routeCode);
        List<TripEntity> trips = tripQueryService.findByAgencyCodeAndLineAndName(agency, route.getLine(), route.getName());
        return sequence(trips, mode);
    }

    public StopGraph sequence(RouteEntity route, TripVariantMode mode) {
        Collection<TripEntity> trips = route == null || route.getTripEntities() == null ? List.of() : route.getTripEntities();
        return sequence(trips, mode);
    }

    public StopGraph sequence(Collection<TripEntity> trips, TripVariantMode mode) {
        Objects.requireNonNull(mode, "mode");
        List<TripEntity> directedTrips = (trips == null ? List.<TripEntity>of() : trips).stream()
                .filter(Objects::nonNull)
                .filter(trip -> mode.equals(trip.getVariantMode()))
                .sorted(Comparator.comparing(TripEntity::getTripId, Comparator.nullsLast(Long::compareTo)))
                .toList();
        if (directedTrips.isEmpty()) {
            return StopGraph.empty(mode);
        }

        List<Long> tripIds = directedTrips.stream()
                .map(TripEntity::getTripId)
                .filter(Objects::nonNull)
                .toList();
        List<StopTimeEntity> stopTimes = tripIds.isEmpty()
                ? List.of()
                : stopTimeRepository.findAllByTripIdIn(tripIds);
        Map<Long, List<StopRef>> stopsByTripId = extractOneSequencePerTrip(stopTimes);

        List<TripStopSequence> sequences = directedTrips.stream()
                .map(trip -> toTripStopSequence(trip, stopsByTripId.getOrDefault(trip.getTripId(), List.of())))
                .filter(sequence -> !sequence.stops().isEmpty())
                .toList();
        return sequence(mode, sequences);
    }

    public StopGraph sequence(TripVariantMode mode, List<TripStopSequence> sequences) {
        Objects.requireNonNull(mode, "mode");
        List<TripStopSequence> tripSequences = (sequences == null ? List.<TripStopSequence>of() : sequences).stream()
                .filter(Objects::nonNull)
                .filter(sequence -> !sequence.stops().isEmpty())
                .toList();
        if (tripSequences.isEmpty()) {
            return StopGraph.empty(mode);
        }

        TripStopSequence spine = selectSpine(tripSequences);
        Map<Long, StopRef> stopsById = collectStops(spine, tripSequences);
        Map<EdgeKey, LinkedHashSet<String>> tripCodesByEdge = collectEdges(tripSequences);

        Map<Long, Set<Long>> outgoing = new LinkedHashMap<>();
        Map<Long, Set<Long>> incoming = new LinkedHashMap<>();
        stopsById.keySet().forEach(stopId -> {
            outgoing.put(stopId, new LinkedHashSet<>());
            incoming.put(stopId, new LinkedHashSet<>());
        });
        tripCodesByEdge.keySet().forEach(edge -> {
            outgoing.get(edge.fromStopId()).add(edge.toStopId());
            incoming.get(edge.toStopId()).add(edge.fromStopId());
        });

        Map<Long, Integer> layers = assignLayers(stopsById.keySet(), outgoing, incoming);
        Map<Long, Integer> spineIndex = spineIndex(spine);

        List<StopGraph.Node> nodes = stopsById.values().stream()
                .sorted(nodeOrder(layers, spineIndex))
                .map(stop -> toNode(stop, incoming, outgoing, layers))
                .toList();
        List<StopGraph.Edge> edges = tripCodesByEdge.entrySet().stream()
                .sorted(edgeOrder(layers))
                .map(entry -> new StopGraph.Edge(
                        entry.getKey().fromStopId(),
                        entry.getKey().toStopId(),
                        entry.getValue().stream().sorted(Comparator.nullsLast(String::compareTo)).toList()))
                .toList();
        List<StopGraph.Branch> branches = tripSequences.stream()
                .filter(sequence -> !Objects.equals(sequence.tripCode(), spine.tripCode()))
                .map(sequence -> align(spine, sequence))
                .flatMap(List::stream)
                .toList();

        return new StopGraph(mode, nodes, edges, spine.stopIds(), spine.tripCode(), branches);
    }

    private static Map<Long, List<StopRef>> extractOneSequencePerTrip(List<StopTimeEntity> stopTimes) {
        Map<Long, Map<Long, List<StopTimeEntity>>> stopTimesByTripAndProfile = new LinkedHashMap<>();
        Map<Long, Map<Long, TripProfileEntity>> profilesByTrip = new LinkedHashMap<>();

        for (StopTimeEntity stopTime : stopTimes) {
            TripProfileEntity profile = stopTime.getTripProfile();
            if (profile == null || profile.getTrip() == null || profile.getTrip().getTripId() == null) {
                continue;
            }
            Long tripId = profile.getTrip().getTripId();
            Long profileId = profile.getTripProfileId();
            stopTimesByTripAndProfile
                    .computeIfAbsent(tripId, unused -> new LinkedHashMap<>())
                    .computeIfAbsent(profileId, unused -> new ArrayList<>())
                    .add(stopTime);
            profilesByTrip
                    .computeIfAbsent(tripId, unused -> new LinkedHashMap<>())
                    .putIfAbsent(profileId, profile);
        }

        Map<Long, List<StopRef>> sequences = new LinkedHashMap<>();
        stopTimesByTripAndProfile.forEach((tripId, byProfile) -> {
            TripProfileEntity selectedProfile = profilesByTrip.get(tripId).values().stream()
                    .min(PROFILE_COMPARATOR)
                    .orElse(null);
            if (selectedProfile == null) {
                return;
            }
            List<StopRef> stops = byProfile.getOrDefault(selectedProfile.getTripProfileId(), List.of()).stream()
                    .sorted(Comparator.comparingInt(stopTime -> stopTime.getStopTimeId().getStopSequence()))
                    .map(StopGraphSequencerService::toStopRef)
                    .flatMap(Optional::stream)
                    .toList();
            sequences.put(tripId, stops);
        });
        return sequences;
    }

    private static Optional<StopRef> toStopRef(StopTimeEntity stopTime) {
        StopEntity stop = stopTime.getStopEntity();
        if (stop == null || stop.getStopId() == null) {
            return Optional.empty();
        }
        return Optional.of(new StopRef(stop.getStopId(), stop.getName(), stop.getLat(), stop.getLon()));
    }

    private static TripStopSequence toTripStopSequence(TripEntity trip, List<StopRef> stops) {
        return new TripStopSequence(
                trip.getTripId(),
                trip.getTripCode(),
                trip.getVariantName(),
                trip.getVariantDesignation(),
                trip.isMainVariant(),
                stops);
    }

    private static TripStopSequence selectSpine(List<TripStopSequence> sequences) {
        return sequences.stream()
                .filter(TripStopSequence::mainVariant)
                .max(Comparator.comparingInt((TripStopSequence sequence) -> sequence.stops().size())
                        .thenComparing(TripStopSequence::tripCode, Comparator.nullsLast(String::compareTo)))
                .orElseGet(() -> sequences.stream()
                        .max(Comparator.comparingInt((TripStopSequence sequence) -> sequence.stops().size())
                                .thenComparing(TripStopSequence::tripCode, Comparator.nullsLast(String::compareTo)))
                        .orElseThrow());
    }

    private static Map<Long, StopRef> collectStops(TripStopSequence spine, List<TripStopSequence> sequences) {
        Map<Long, StopRef> stopsById = new LinkedHashMap<>();
        spine.stops().forEach(stop -> stopsById.putIfAbsent(stop.stopId(), stop));
        sequences.forEach(sequence -> sequence.stops().forEach(stop -> stopsById.putIfAbsent(stop.stopId(), stop)));
        return stopsById;
    }

    private static Map<EdgeKey, LinkedHashSet<String>> collectEdges(List<TripStopSequence> sequences) {
        Map<EdgeKey, LinkedHashSet<String>> tripCodesByEdge = new LinkedHashMap<>();
        for (TripStopSequence sequence : sequences) {
            List<StopRef> stops = sequence.stops();
            for (int i = 0; i < stops.size() - 1; i++) {
                Long from = stops.get(i).stopId();
                Long to = stops.get(i + 1).stopId();
                if (from == null || to == null || from.equals(to)) {
                    continue;
                }
                tripCodesByEdge
                        .computeIfAbsent(new EdgeKey(from, to), unused -> new LinkedHashSet<>())
                        .add(sequence.tripCode());
            }
        }
        return tripCodesByEdge;
    }

    private static Map<Long, Integer> assignLayers(
            Set<Long> nodeIds,
            Map<Long, Set<Long>> outgoing,
            Map<Long, Set<Long>> incoming) {
        Map<Long, Integer> remainingInDegree = nodeIds.stream()
                .collect(Collectors.toMap(id -> id, id -> incoming.getOrDefault(id, Set.of()).size(), (a, b) -> a, LinkedHashMap::new));
        Map<Long, Integer> layers = new LinkedHashMap<>();
        Queue<Long> queue = new ArrayDeque<>();

        remainingInDegree.forEach((id, degree) -> {
            if (degree == 0) {
                layers.put(id, 0);
                queue.add(id);
            }
        });

        int processed = 0;
        while (!queue.isEmpty()) {
            Long current = queue.remove();
            processed++;
            int currentLayer = layers.getOrDefault(current, 0);
            for (Long next : outgoing.getOrDefault(current, Set.of())) {
                int candidate = currentLayer + 1;
                Integer existing = layers.get(next);
                if (existing == null || candidate > existing) {
                    layers.put(next, candidate);
                }
                int left = remainingInDegree.merge(next, -1, Integer::sum);
                if (left == 0) {
                    queue.add(next);
                }
            }
        }

        if (processed < nodeIds.size()) {
            int fallbackLayer = layers.values().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            nodeIds.stream()
                    .filter(id -> !layers.containsKey(id))
                    .forEach(id -> layers.put(id, fallbackLayer));
        }
        return layers;
    }

    private static Map<Long, Integer> spineIndex(TripStopSequence spine) {
        Map<Long, Integer> index = new LinkedHashMap<>();
        List<Long> stopIds = spine.stopIds();
        for (int i = 0; i < stopIds.size(); i++) {
            index.putIfAbsent(stopIds.get(i), i);
        }
        return index;
    }

    private static Comparator<StopRef> nodeOrder(Map<Long, Integer> layers, Map<Long, Integer> spineIndex) {
        return Comparator
                .comparingInt((StopRef stop) -> layers.getOrDefault(stop.stopId(), Integer.MAX_VALUE))
                .thenComparingInt(stop -> spineIndex.getOrDefault(stop.stopId(), Integer.MAX_VALUE))
                .thenComparing(StopRef::stopId, Comparator.nullsLast(Long::compareTo));
    }

    private static Comparator<Map.Entry<EdgeKey, LinkedHashSet<String>>> edgeOrder(Map<Long, Integer> layers) {
        return Comparator
                .comparingInt((Map.Entry<EdgeKey, LinkedHashSet<String>> entry) -> layers.getOrDefault(entry.getKey().fromStopId(), Integer.MAX_VALUE))
                .thenComparingInt(entry -> layers.getOrDefault(entry.getKey().toStopId(), Integer.MAX_VALUE))
                .thenComparing(entry -> entry.getKey().fromStopId(), Comparator.nullsLast(Long::compareTo))
                .thenComparing(entry -> entry.getKey().toStopId(), Comparator.nullsLast(Long::compareTo));
    }

    private static StopGraph.Node toNode(
            StopRef stop,
            Map<Long, Set<Long>> incoming,
            Map<Long, Set<Long>> outgoing,
            Map<Long, Integer> layers) {
        int inDegree = incoming.getOrDefault(stop.stopId(), Set.of()).size();
        int outDegree = outgoing.getOrDefault(stop.stopId(), Set.of()).size();
        return new StopGraph.Node(
                stop.stopId(),
                stop.name(),
                stop.lat(),
                stop.lon(),
                inDegree,
                outDegree,
                layers.getOrDefault(stop.stopId(), 0),
                roles(inDegree, outDegree));
    }

    private static Set<StopGraph.NodeRole> roles(int inDegree, int outDegree) {
        EnumSet<StopGraph.NodeRole> roles = EnumSet.noneOf(StopGraph.NodeRole.class);
        if (inDegree == 0 && outDegree == 0) {
            roles.add(StopGraph.NodeRole.ISOLATED);
            return roles;
        }
        if (inDegree == 0) {
            roles.add(StopGraph.NodeRole.ORIGIN);
        }
        if (outDegree == 0) {
            roles.add(StopGraph.NodeRole.DESTINATION);
        }
        if (outDegree > 1) {
            roles.add(StopGraph.NodeRole.FORK);
        }
        if (inDegree > 1) {
            roles.add(StopGraph.NodeRole.JOIN);
        }
        if (inDegree == 1 && outDegree == 1) {
            roles.add(StopGraph.NodeRole.TRUNK);
        }
        return roles;
    }

    private static List<StopGraph.Branch> align(TripStopSequence spine, TripStopSequence variant) {
        List<Long> main = spine.stopIds();
        List<Long> other = variant.stopIds();
        if (main.equals(other)) {
            return List.of();
        }

        int prefixLength = 0;
        int maxShared = Math.min(main.size(), other.size());
        while (prefixLength < maxShared && Objects.equals(main.get(prefixLength), other.get(prefixLength))) {
            prefixLength++;
        }

        int suffixLength = 0;
        int maxSuffix = maxShared - prefixLength;
        while (suffixLength < maxSuffix
                && Objects.equals(main.get(main.size() - 1 - suffixLength), other.get(other.size() - 1 - suffixLength))) {
            suffixLength++;
        }

        List<Long> branchStops = other.subList(prefixLength, other.size() - suffixLength);
        Long divergeFromStopId = prefixLength == 0 ? null : other.get(prefixLength - 1);
        Long rejoinAtStopId = suffixLength == 0 ? null : other.get(other.size() - suffixLength);
        StopGraph.BranchType type = branchType(main.size(), other.size(), prefixLength, suffixLength, branchStops);

        return List.of(new StopGraph.Branch(
                variant.tripCode(),
                variant.variantName(),
                variant.variantDesignation(),
                type,
                divergeFromStopId,
                rejoinAtStopId,
                branchStops));
    }

    private static StopGraph.BranchType branchType(
            int mainLength,
            int variantLength,
            int prefixLength,
            int suffixLength,
            List<Long> branchStops) {
        if (prefixLength == variantLength && variantLength < mainLength) {
            return StopGraph.BranchType.SHORTCUT;
        }
        if (prefixLength == mainLength && mainLength < variantLength) {
            return StopGraph.BranchType.EXTENSION;
        }
        if (prefixLength > 0 && suffixLength > 0 && !branchStops.isEmpty()) {
            return StopGraph.BranchType.DETOUR;
        }
        if (prefixLength == 0 && suffixLength > 0) {
            return StopGraph.BranchType.DIFFERENT_ORIGIN;
        }
        if (prefixLength > 0 && suffixLength == 0) {
            return StopGraph.BranchType.DIFFERENT_DESTINATION;
        }
        return StopGraph.BranchType.ALTERNATE;
    }

    private static int trafficModeWeight(TripProfileEntity profile) {
        TripTrafficMode trafficMode = profile.getTrafficMode();
        return trafficMode == null ? Integer.MIN_VALUE : trafficMode.getWeight();
    }

    private record EdgeKey(Long fromStopId, Long toStopId) {
    }
}
