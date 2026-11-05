package pl.wrona.webserver.bussiness.route.details;

import org.igeolab.iot.pt.server.api.model.RouteDetails;
import org.igeolab.iot.pt.server.api.model.TerritoryUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.bussiness.stop.name.AgencyStopNameService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.StopTimeService;
import pl.wrona.webserver.core.TerritorialUnitQueryService;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.core.entity.TerritorialUnitEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteDetailsServiceTest {

    @Mock
    private RouteQueryService routeQueryService;
    @Mock
    private TripQueryService tripQueryService;
    @Mock
    private StopTimeService stopTimeService;
    @Mock
    private StopService stopService;
    @Mock
    private TerritorialUnitQueryService territorialUnitQueryService;
    @Mock
    private AgencyStopNameService agencyStopNameService;

    @InjectMocks
    private RouteDetailsService routeDetailsService;

    @Test
    void listsEachTerritoryOnceInTripOrderWithoutLoadingStopsOfTheTerritory() {
        RouteEntity route = route();
        StopEntity chmielnik = stop(10L, 100L);
        StopEntity sladkow = stop(20L, 100L);
        StopEntity pinczow = stop(30L, 200L);
        TripEntity mainFront = mainFront(route);

        when(routeQueryService.findRouteByAgencyCodeAndRouteCode("WAWA", "00001")).thenReturn(route);
        when(tripQueryService.mapByExistsBrigade("WAWA", route)).thenReturn(Map.of());
        when(tripQueryService.findByAgencyCodeAndLineAndName("WAWA", "L1", "Chmielnik - Pińczów")).thenReturn(List.of(mainFront));
        when(agencyStopNameService.findOverrides(eq("WAWA"), any())).thenReturn(Map.of());
        when(stopTimeService.getAllStopTimesByTrip(mainFront)).thenReturn(List.of(
                stopTime(chmielnik),
                stopTime(sladkow),
                stopTime(pinczow),
                stopTime(chmielnik)
        ));
        when(stopService.mapStopByIdsIn(anyList())).thenReturn(Map.of(
                10L, chmielnik,
                20L, sladkow,
                30L, pinczow
        ));
        when(territorialUnitQueryService.findAllByTerritoriesIdIn(List.of(100L, 200L))).thenReturn(List.of(
                territory(100L, "Chmielnik"),
                territory(200L, "Pińczów")
        ));

        RouteDetails details = routeDetailsService.getRouteDetails("WAWA", "00001");

        assertThat(details.getTerritories()).extracting(TerritoryUnit::getName).containsExactly("Chmielnik", "Pińczów");
        assertThat(details.getRoute().getOriginTerritory().getName()).isEqualTo("Chmielnik");
        assertThat(details.getRoute().getDestinationTerritory().getName()).isEqualTo("Pińczów");
        verify(territorialUnitQueryService, never()).findAllByStopIdIn(anyList());
    }

    @Test
    void keepsOriginAndDestinationTerritoriesWhenTheLineHasNoMainTrip() {
        RouteEntity route = route();
        StopEntity chmielnik = stop(10L, 100L);
        StopEntity pinczow = stop(30L, 200L);

        when(routeQueryService.findRouteByAgencyCodeAndRouteCode("WAWA", "00001")).thenReturn(route);
        when(tripQueryService.mapByExistsBrigade("WAWA", route)).thenReturn(Map.of());
        when(tripQueryService.findByAgencyCodeAndLineAndName("WAWA", "L1", "Chmielnik - Pińczów")).thenReturn(List.of());
        when(agencyStopNameService.findOverrides(eq("WAWA"), any())).thenReturn(Map.of());
        when(stopService.mapStopByIdsIn(anyList())).thenReturn(Map.of(10L, chmielnik, 30L, pinczow));
        when(territorialUnitQueryService.findAllByTerritoriesIdIn(List.of(100L, 200L))).thenReturn(List.of(
                territory(100L, "Chmielnik"),
                territory(200L, "Pińczów")
        ));

        RouteDetails details = routeDetailsService.getRouteDetails("WAWA", "00001");

        assertThat(details.getTerritories()).isEmpty();
        assertThat(details.getRoute().getOriginTerritory().getName()).isEqualTo("Chmielnik");
        assertThat(details.getRoute().getDestinationTerritory().getName()).isEqualTo("Pińczów");
        verify(territorialUnitQueryService, never()).findAllByStopIdIn(any());
    }

    private static RouteEntity route() {
        RouteEntity route = new RouteEntity();
        route.setRouteId(1L);
        route.setLine("L1");
        route.setName("Chmielnik - Pińczów");
        route.setVersion(1);
        route.setRouteCode("00001");
        route.setOriginStopId(10L);
        route.setOriginStopName("Chmielnik");
        route.setDestinationStopId(30L);
        route.setDestinationStopName("Pińczów");
        return route;
    }

    private static TripEntity mainFront(RouteEntity route) {
        return TripEntity.builder()
                .tripId(7L)
                .route(route)
                .mainVariant(true)
                .variantMode(TripVariantMode.FRONT)
                .originStopId("10")
                .destinationStopId("30")
                .tripProfiles(Set.of())
                .build();
    }

    private static StopEntity stop(long stopId, long territoryId) {
        return StopEntity.builder()
                .stopId(stopId)
                .territorialUnitId(territoryId)
                .lat(50.0)
                .lon(20.0)
                .build();
    }

    private static StopTimeEntity stopTime(StopEntity stop) {
        return StopTimeEntity.builder().stopEntity(stop).build();
    }

    private static TerritorialUnitEntity territory(long id, String name) {
        return TerritorialUnitEntity.builder()
                .territorialUnitId(id)
                .nazwa(name)
                .build();
    }
}
