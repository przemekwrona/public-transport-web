package pl.wrona.webserver.bussiness.stop.usage;

import org.igeolab.iot.pt.server.api.model.AgencyStop;
import org.igeolab.iot.pt.server.api.model.AgencyStops;
import org.igeolab.iot.pt.server.api.model.TripMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.core.entity.StopEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgencyStopUsageServiceTest {

    @Mock
    private StopTimeRepository stopTimeRepository;

    @InjectMocks
    private AgencyStopUsageService agencyStopUsageService;

    @Test
    void groupsStopTimesIntoUniqueStopsRoutesAndTrips() {
        RouteEntity lineTwelve = route(1L, "A1", "12", "Centrum");
        RouteEntity lineFive = route(2L, "B2", "5", "Dworzec");
        StopEntity market = stop(10L, "Rynek");
        StopEntity station = stop(20L, "Dworzec");
        TripEntity outbound = trip(7L, "001", TripVariantMode.FRONT, "Dworzec", lineTwelve);
        TripEntity inbound = trip(8L, "002", TripVariantMode.BACK, "Rynek", lineFive);

        when(stopTimeRepository.findAllUsedByAgencyCode("WAWA")).thenReturn(List.of(
                stopTime(1L, 1, outbound, market),
                stopTime(2L, 1, outbound, market),
                stopTime(3L, 2, inbound, market),
                stopTime(4L, 1, inbound, station)
        ));

        AgencyStops response = agencyStopUsageService.getAgencyStops("WAWA");

        assertThat(response.getItems()).extracting(item -> item.getStop().getName())
                .containsExactly("Dworzec", "Rynek");

        AgencyStop marketUsage = response.getItems().get(1);
        assertThat(marketUsage.getRoutes()).extracting(route -> route.getLine()).containsExactly("12", "5");
        assertThat(marketUsage.getTrips()).extracting(trip -> trip.getTripCode()).containsExactly("001", "002");
        assertThat(marketUsage.getTrips().get(0).getVariantMode()).isEqualTo(TripMode.FRONT);
        assertThat(marketUsage.getTrips().get(0).getHeadsign()).isEqualTo("Dworzec");

        AgencyStop stationUsage = response.getItems().get(0);
        assertThat(stationUsage.getRoutes()).extracting(route -> route.getRouteCode()).containsExactly("B2");
        assertThat(stationUsage.getTrips()).hasSize(1);
    }

    @Test
    void returnsEmptyListWhenAgencyHasNoStopTimes() {
        when(stopTimeRepository.findAllUsedByAgencyCode("WAWA")).thenReturn(List.of());

        AgencyStops response = agencyStopUsageService.getAgencyStops("WAWA");

        assertThat(response.getItems()).isEmpty();
    }

    private static RouteEntity route(Long id, String code, String line, String name) {
        RouteEntity route = new RouteEntity();
        route.setRouteId(id);
        route.setRouteCode(code);
        route.setLine(line);
        route.setName(name);
        return route;
    }

    private static TripEntity trip(Long id, String code, TripVariantMode mode, String headsign, RouteEntity route) {
        return TripEntity.builder()
                .tripId(id)
                .tripCode(code)
                .variantMode(mode)
                .headsign(headsign)
                .route(route)
                .build();
    }

    private static StopEntity stop(Long id, String name) {
        return StopEntity.builder()
                .stopId(id)
                .name(name)
                .lat(52.2)
                .lon(21.0)
                .build();
    }

    private static StopTimeEntity stopTime(Long profileId, int sequence, TripEntity trip, StopEntity stop) {
        TripProfileEntity profile = new TripProfileEntity();
        profile.setTripProfileId(profileId);
        profile.setTrip(trip);

        StopTimeEntity stopTime = new StopTimeEntity();
        stopTime.setStopTimeId(new StopTimeId(profileId, sequence));
        stopTime.setTripProfile(profile);
        stopTime.setStopEntity(stop);
        return stopTime;
    }
}
