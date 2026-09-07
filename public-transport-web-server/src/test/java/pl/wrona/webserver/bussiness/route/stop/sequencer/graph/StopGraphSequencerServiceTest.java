package pl.wrona.webserver.bussiness.route.stop.sequencer.graph;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.bussiness.route.stop.sequencer.StopGraph;
import pl.wrona.webserver.bussiness.route.stop.sequencer.StopGraphSequencerService;
import pl.wrona.webserver.bussiness.route.stop.sequencer.StopRef;
import pl.wrona.webserver.bussiness.route.stop.sequencer.TripStopSequence;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.agency.TripTrafficMode;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.core.entity.StopEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StopGraphSequencerServiceTest {

    @Mock
    private RouteQueryService routeQueryService;
    @Mock
    private TripQueryService tripQueryService;
    @Mock
    private StopTimeRepository stopTimeRepository;

    @InjectMocks
    private StopGraphSequencerService sequencer;

    @Test
    void emptySequencesYieldEmptyGraph() {
        StopGraph graph = sequencer.sequence(TripVariantMode.FRONT, List.of());

        assertThat(graph.mode()).isEqualTo(TripVariantMode.FRONT);
        assertThat(graph.nodes()).isEmpty();
        assertThat(graph.edges()).isEmpty();
        assertThat(graph.spineStopIds()).isEmpty();
        assertThat(graph.spineTripCode()).isNull();
        assertThat(graph.branches()).isEmpty();
    }

    @Test
    void linearMainTripHasOriginTrunkDestinationAndNoBranches() {
        TripStopSequence main = sequence(1L, "MAIN", true, 1L, 2L, 3L);

        StopGraph graph = sequencer.sequence(TripVariantMode.FRONT, List.of(main));

        assertThat(graph.spineTripCode()).isEqualTo("T1");
        assertThat(graph.spineStopIds()).containsExactly(1L, 2L, 3L);
        assertThat(graph.branches()).isEmpty();
        assertThat(node(graph, 1L).roles()).containsExactly(StopGraph.NodeRole.ORIGIN);
        assertThat(node(graph, 2L).roles()).containsExactly(StopGraph.NodeRole.TRUNK);
        assertThat(node(graph, 3L).roles()).containsExactly(StopGraph.NodeRole.DESTINATION);
        assertThat(graph.edges()).extracting(StopGraph.Edge::fromStopId, StopGraph.Edge::toStopId)
                .containsExactly(tuple(1L, 2L), tuple(2L, 3L));
        assertThat(graph.edges()).allMatch(edge -> edge.tripCodes().equals(List.of("T1")));
    }

    @Test
    void detourCreatesForkJoinAndBranch() {
        TripStopSequence main = sequence(1L, "MAIN", true, 1L, 2L, 3L, 4L, 5L);
        TripStopSequence viaSchool = sequence(2L, "SZKOLA", false, 1L, 2L, 10L, 4L, 5L);

        StopGraph graph = sequencer.sequence(TripVariantMode.FRONT, List.of(main, viaSchool));

        assertThat(node(graph, 2L).roles()).containsExactly(StopGraph.NodeRole.FORK);
        assertThat(node(graph, 2L).outDegree()).isEqualTo(2);
        assertThat(node(graph, 4L).roles()).containsExactly(StopGraph.NodeRole.JOIN);
        assertThat(node(graph, 4L).inDegree()).isEqualTo(2);
        assertThat(node(graph, 10L).roles()).containsExactly(StopGraph.NodeRole.TRUNK);
        assertThat(graph.edges()).extracting(StopGraph.Edge::fromStopId, StopGraph.Edge::toStopId)
                .contains(tuple(2L, 3L), tuple(2L, 10L), tuple(10L, 4L));
        assertThat(graph.branches()).containsExactly(new StopGraph.Branch(
                "T2", "SZKOLA", "S", StopGraph.BranchType.DETOUR, 2L, 4L, List.of(10L)));
    }

    @Test
    void shortcutIsClassifiedAgainstMainSpine() {
        TripStopSequence main = sequence(1L, "MAIN", true, 1L, 2L, 3L, 4L);
        TripStopSequence shortTrip = sequence(2L, "SKROT", false, 1L, 2L, 3L);

        StopGraph graph = sequencer.sequence(TripVariantMode.FRONT, List.of(main, shortTrip));

        assertThat(graph.branches()).containsExactly(new StopGraph.Branch(
                "T2", "SKROT", "S", StopGraph.BranchType.SHORTCUT, 3L, null, List.of()));
        assertThat(node(graph, 3L).roles()).containsExactly(StopGraph.NodeRole.TRUNK);
        assertThat(node(graph, 4L).roles()).containsExactly(StopGraph.NodeRole.DESTINATION);
    }

    @Test
    void extensionContinuesPastMainDestination() {
        TripStopSequence main = sequence(1L, "MAIN", true, 1L, 2L, 3L);
        TripStopSequence longer = sequence(2L, "DALEJ", false, 1L, 2L, 3L, 4L, 5L);

        StopGraph graph = sequencer.sequence(TripVariantMode.FRONT, List.of(main, longer));

        assertThat(graph.branches()).containsExactly(new StopGraph.Branch(
                "T2", "DALEJ", "S", StopGraph.BranchType.EXTENSION, 3L, null, List.of(4L, 5L)));
        assertThat(node(graph, 3L).roles()).containsExactly(StopGraph.NodeRole.TRUNK);
        assertThat(node(graph, 5L).roles()).containsExactly(StopGraph.NodeRole.DESTINATION);
    }

    @Test
    void differentOriginRejoinsTheSpine() {
        TripStopSequence main = sequence(1L, "MAIN", true, 1L, 2L, 3L, 4L, 5L);
        TripStopSequence fromDepot = sequence(2L, "ZAJEZDNIA", false, 20L, 21L, 3L, 4L, 5L);

        StopGraph graph = sequencer.sequence(TripVariantMode.FRONT, List.of(main, fromDepot));

        assertThat(graph.branches()).containsExactly(new StopGraph.Branch(
                "T2", "ZAJEZDNIA", "S", StopGraph.BranchType.DIFFERENT_ORIGIN, null, 3L, List.of(20L, 21L)));
        assertThat(node(graph, 1L).roles()).containsExactly(StopGraph.NodeRole.ORIGIN);
        assertThat(node(graph, 20L).roles()).containsExactly(StopGraph.NodeRole.ORIGIN);
        assertThat(node(graph, 3L).roles()).containsExactly(StopGraph.NodeRole.JOIN);
    }

    @Test
    void differentDestinationLeavesTheSpine() {
        TripStopSequence main = sequence(1L, "MAIN", true, 1L, 2L, 3L, 4L, 5L);
        TripStopSequence otherEnd = sequence(2L, "OSIEDLE", false, 1L, 2L, 8L, 9L);

        StopGraph graph = sequencer.sequence(TripVariantMode.FRONT, List.of(main, otherEnd));

        assertThat(graph.branches()).containsExactly(new StopGraph.Branch(
                "T2", "OSIEDLE", "S", StopGraph.BranchType.DIFFERENT_DESTINATION, 2L, null, List.of(8L, 9L)));
        assertThat(node(graph, 2L).roles()).containsExactly(StopGraph.NodeRole.FORK);
        assertThat(node(graph, 5L).roles()).containsExactly(StopGraph.NodeRole.DESTINATION);
        assertThat(node(graph, 9L).roles()).containsExactly(StopGraph.NodeRole.DESTINATION);
    }

    @Test
    void ignoresOppositeDirectionTrips() {
        TripEntity front = trip(1L, TripVariantMode.FRONT, true);
        TripEntity back = trip(2L, TripVariantMode.BACK, true);
        when(stopTimeRepository.findAllByTripIdIn(List.of(1L))).thenReturn(stopTimes(front, 1L, 1L, 2L, 3L));

        StopGraph graph = sequencer.sequence(List.of(front, back), TripVariantMode.FRONT);

        assertThat(graph.mode()).isEqualTo(TripVariantMode.FRONT);
        assertThat(graph.spineStopIds()).containsExactly(1L, 2L, 3L);
        assertThat(graph.nodes()).extracting(StopGraph.Node::stopId).containsExactly(1L, 2L, 3L);
        verify(stopTimeRepository).findAllByTripIdIn(List.of(1L));
    }

    @Test
    void usesOneStopSequenceWhenTripHasMultipleProfiles() {
        TripEntity trip = trip(1L, TripVariantMode.FRONT, true);
        TripProfileEntity normal = profile(trip, 10L, TripTrafficMode.NORMAL, true);
        TripProfileEntity traffic = profile(trip, 11L, TripTrafficMode.TRAFFIC, false);
        List<StopTimeEntity> stopTimes = List.of(
                stopTime(normal, 1, stop(1L)),
                stopTime(normal, 2, stop(2L)),
                stopTime(normal, 3, stop(3L)),
                stopTime(traffic, 1, stop(1L)),
                stopTime(traffic, 2, stop(2L)),
                stopTime(traffic, 3, stop(3L)));
        when(stopTimeRepository.findAllByTripIdIn(List.of(1L))).thenReturn(stopTimes);

        StopGraph graph = sequencer.sequence(List.of(trip), TripVariantMode.FRONT);

        assertThat(graph.edges()).hasSize(2);
        assertThat(graph.edges()).allMatch(edge -> edge.tripCodes().equals(List.of("T1")));
        assertThat(graph.spineStopIds()).containsExactly(1L, 2L, 3L);
    }

    @Test
    void prefersMainVariantAsSpineEvenWhenShorter() {
        TripStopSequence main = sequence(1L, "MAIN", true, 1L, 2L, 3L);
        TripStopSequence longer = sequence(2L, "DALEJ", false, 1L, 2L, 3L, 4L);

        StopGraph graph = sequencer.sequence(TripVariantMode.BACK, List.of(longer, main));

        assertThat(graph.mode()).isEqualTo(TripVariantMode.BACK);
        assertThat(graph.spineTripCode()).isEqualTo("T1");
        assertThat(graph.spineStopIds()).containsExactly(1L, 2L, 3L);
    }

    @Test
    void doesNotQueryRepositoryWhenNoDirectedTrips() {
        TripEntity back = trip(2L, TripVariantMode.BACK, true);

        StopGraph graph = sequencer.sequence(List.of(back), TripVariantMode.FRONT);

        assertThat(graph).isEqualTo(StopGraph.empty(TripVariantMode.FRONT));
        verify(stopTimeRepository, never()).findAllByTripIdIn(any());
    }

    private static StopGraph.Node node(StopGraph graph, Long stopId) {
        return graph.nodes().stream()
                .filter(candidate -> stopId.equals(candidate.stopId()))
                .findFirst()
                .orElseThrow();
    }

    private static TripStopSequence sequence(Long tripId, String variantName, boolean main, Long... stopIds) {
        List<StopRef> stops = Arrays.stream(stopIds)
                .map(stopId -> new StopRef(stopId, "Stop " + stopId, stopId.doubleValue(), stopId.doubleValue()))
                .toList();
        return new TripStopSequence(tripId, "T" + tripId, variantName, "S", main, stops);
    }

    private static TripEntity trip(Long tripId, TripVariantMode mode, boolean mainVariant) {
        return TripEntity.builder()
                .tripId(tripId)
                .tripCode("T" + tripId)
                .variantName("MAIN")
                .variantMode(mode)
                .mainVariant(mainVariant)
                .build();
    }

    private static List<StopTimeEntity> stopTimes(TripEntity trip, Long profileId, Long... stopIds) {
        TripProfileEntity profile = profile(trip, profileId, TripTrafficMode.NORMAL, true);
        List<StopTimeEntity> stopTimes = new ArrayList<>();
        for (int i = 0; i < stopIds.length; i++) {
            stopTimes.add(stopTime(profile, i + 1, stop(stopIds[i])));
        }
        return stopTimes;
    }

    private static TripProfileEntity profile(TripEntity trip, Long profileId, TripTrafficMode trafficMode, boolean defaultProfile) {
        return TripProfileEntity.builder()
                .tripProfileId(profileId)
                .trip(trip)
                .trafficMode(trafficMode)
                .defaultProfile(defaultProfile)
                .build();
    }

    private static StopTimeEntity stopTime(TripProfileEntity profile, int sequence, StopEntity stop) {
        return StopTimeEntity.builder()
                .stopTimeId(new StopTimeId(profile.getTripProfileId(), sequence))
                .tripProfile(profile)
                .stopEntity(stop)
                .build();
    }

    private static StopEntity stop(Long stopId) {
        return StopEntity.builder()
                .stopId(stopId)
                .name("Stop " + stopId)
                .lat(stopId.doubleValue())
                .lon(stopId.doubleValue())
                .build();
    }
}
