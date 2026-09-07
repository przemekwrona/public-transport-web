package pl.wrona.webserver.bussiness.route.stop;

import org.igeolab.iot.pt.server.api.model.RouteStopGraphBranchType;
import org.igeolab.iot.pt.server.api.model.RouteStopGraphNodeRole;
import org.igeolab.iot.pt.server.api.model.RouteStops;
import org.igeolab.iot.pt.server.api.model.TripMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.bussiness.route.stop.sequencer.RouteStopsService;
import pl.wrona.webserver.bussiness.route.stop.sequencer.StopGraphSequencerService;
import pl.wrona.webserver.bussiness.route.stop.sequencer.StopGraph;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteStopsServiceTest {

    @Mock
    private RouteQueryService routeQueryService;
    @Mock
    private TripQueryService tripQueryService;
    @Mock
    private StopGraphSequencerService stopGraphSequencerService;

    @InjectMocks
    private RouteStopsService routeStopsService;

    @Test
    void getRouteStopsMapsFrontAndBackGraphs() {
        var route = new RouteEntity();
        List<TripEntity> trips = List.of(TripEntity.builder().tripId(1L).build());
        var front = new StopGraph(
                TripVariantMode.FRONT,
                List.of(new StopGraph.Node(10L, "A", 52.1, 21.1, 0, 1, 0, Set.of(StopGraph.NodeRole.ORIGIN))),
                List.of(new StopGraph.Edge(10L, 11L, List.of("T1"))),
                List.of(10L, 11L),
                "T1",
                List.of());
        var back = new StopGraph(
                TripVariantMode.BACK,
                List.of(new StopGraph.Node(11L, "B", 52.2, 21.2, 0, 1, 0, Set.of(StopGraph.NodeRole.ORIGIN))),
                List.of(),
                List.of(11L),
                "T2",
                List.of(new StopGraph.Branch(
                        "T3", "SZKOLA", "S", StopGraph.BranchType.DETOUR, 11L, 10L, List.of(99L))));

        when(routeQueryService.findRouteByAgencyCodeAndRouteCode("WAWA", "R1")).thenReturn(route);
        when(tripQueryService.findByAgencyCodeAndRoute("WAWA", route)).thenReturn(trips);
        when(stopGraphSequencerService.sequence(trips, TripVariantMode.FRONT)).thenReturn(front);
        when(stopGraphSequencerService.sequence(trips, TripVariantMode.BACK)).thenReturn(back);

        RouteStops response = routeStopsService.getRouteStops("WAWA", "R1");

        assertThat(response.getFront().getMode()).isEqualTo(TripMode.FRONT);
        assertThat(response.getFront().getSpineTripCode()).isEqualTo("T1");
        assertThat(response.getFront().getSpineStopIds()).containsExactly(10L, 11L);
        assertThat(response.getFront().getNodes()).hasSize(1);
        assertThat(response.getFront().getNodes().get(0).getStopId()).isEqualTo(10L);
        assertThat(response.getFront().getNodes().get(0).getRoles()).containsExactly(RouteStopGraphNodeRole.ORIGIN);
        assertThat(response.getFront().getEdges().get(0).getFromStopId()).isEqualTo(10L);
        assertThat(response.getFront().getEdges().get(0).getToStopId()).isEqualTo(11L);
        assertThat(response.getBack().getMode()).isEqualTo(TripMode.BACK);
        assertThat(response.getBack().getBranches()).hasSize(1);
        assertThat(response.getBack().getBranches().get(0).getType()).isEqualTo(RouteStopGraphBranchType.DETOUR);
        assertThat(response.getBack().getBranches().get(0).getStopIds()).containsExactly(99L);
        verify(stopGraphSequencerService).sequence(trips, TripVariantMode.FRONT);
        verify(stopGraphSequencerService).sequence(trips, TripVariantMode.BACK);
    }
}
