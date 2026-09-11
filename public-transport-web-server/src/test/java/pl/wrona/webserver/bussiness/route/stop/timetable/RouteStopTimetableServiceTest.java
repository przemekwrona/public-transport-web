package pl.wrona.webserver.bussiness.route.stop.timetable;

import org.igeolab.iot.pt.server.api.model.RouteStopTimetable;
import org.igeolab.iot.pt.server.api.model.TripMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.exception.BusinessException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteStopTimetableServiceTest {

    @Mock
    private TripQueryService tripQueryService;
    @Mock
    private BrigadeEventQueryService brigadeEventQueryService;
    @Mock
    private StopTimeRepository stopTimeRepository;

    @InjectMocks
    private RouteStopTimetableService routeStopTimetableService;

    @Test
    void returnsEmptyDeparturesWhenRouteHasNoTripsInMode() {
        when(tripQueryService.findByAgencyAndRouteCodeAndVariantMode("WAWA", "R1", TripVariantMode.FRONT))
                .thenReturn(List.of());

        RouteStopTimetable response = routeStopTimetableService.getRouteStopTimetable("WAWA", "R1", "10", TripMode.FRONT);

        assertThat(response.getStopCode()).isEqualTo("10");
        assertThat(response.getTripMode()).isEqualTo(TripMode.FRONT);
        assertThat(response.getDepartures()).isEmpty();
        verify(stopTimeRepository, never()).findAllByTripIdInAndStopId(anyCollection(), any());
        verify(brigadeEventQueryService, never()).findAllByTripIds(anyCollection());
    }

    @Test
    void returnsDeparturesAtStopForTripsInRequestedMode() {
        TripEntity trip = trip(1L, "T1", "S");
        TripProfileEntity profile = profile(11L, trip);
        StopTimeEntity stopTime = stopTime(profile, 1, 300);
        BrigadeEventEntity event = event(profile, 6 * 3600);

        when(tripQueryService.findByAgencyAndRouteCodeAndVariantMode("WAWA", "R1", TripVariantMode.BACK))
                .thenReturn(List.of(trip));
        when(stopTimeRepository.findAllByTripIdInAndStopId(List.of(1L), 10L)).thenReturn(List.of(stopTime));
        when(brigadeEventQueryService.findAllByTripIds(List.of(1L))).thenReturn(List.of(event));

        RouteStopTimetable response = routeStopTimetableService.getRouteStopTimetable("WAWA", "R1", "10", TripMode.BACK);

        assertThat(response.getTripMode()).isEqualTo(TripMode.BACK);
        assertThat(response.getDepartures()).hasSize(1);
        assertThat(response.getDepartures().get(0).getH()).isEqualTo(6);
        assertThat(response.getDepartures().get(0).getM()).isEqualTo(5);
        assertThat(response.getDepartures().get(0).getTime()).isEqualTo("06:05");
        assertThat(response.getDepartures().get(0).getSymbol()).isEqualTo("S");
        assertThat(response.getDepartures().get(0).getTripCode()).isEqualTo("T1");
    }

    @Test
    void skipsEventsWhoseProfileDoesNotServeTheStop() {
        TripEntity trip = trip(1L, "T1", null);
        TripProfileEntity served = profile(11L, trip);
        TripProfileEntity other = profile(12L, trip);

        when(tripQueryService.findByAgencyAndRouteCodeAndVariantMode("WAWA", "R1", TripVariantMode.FRONT))
                .thenReturn(List.of(trip));
        when(stopTimeRepository.findAllByTripIdInAndStopId(List.of(1L), 10L)).thenReturn(List.of(stopTime(served, 1, 0)));
        when(brigadeEventQueryService.findAllByTripIds(List.of(1L))).thenReturn(List.of(event(other, 7 * 3600)));

        RouteStopTimetable response = routeStopTimetableService.getRouteStopTimetable("WAWA", "R1", "10", TripMode.FRONT);

        assertThat(response.getDepartures()).isEmpty();
    }

    @Test
    void rejectsNonNumericStopCode() {
        assertThatThrownBy(() -> routeStopTimetableService.getRouteStopTimetable("WAWA", "R1", "ABC", TripMode.FRONT))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ABC");
    }

    private static TripEntity trip(Long tripId, String tripCode, String designation) {
        return TripEntity.builder()
                .tripId(tripId)
                .tripCode(tripCode)
                .variantDesignation(designation)
                .variantMode(TripVariantMode.FRONT)
                .build();
    }

    private static TripProfileEntity profile(Long profileId, TripEntity trip) {
        return TripProfileEntity.builder()
                .tripProfileId(profileId)
                .trip(trip)
                .build();
    }

    private static StopTimeEntity stopTime(TripProfileEntity profile, int sequence, int customizedSeconds) {
        return StopTimeEntity.builder()
                .stopTimeId(new StopTimeId(profile.getTripProfileId(), sequence))
                .tripProfile(profile)
                .customizedTimeSeconds(customizedSeconds)
                .build();
    }

    private static BrigadeEventEntity event(TripProfileEntity profile, int startSecond) {
        BrigadeEventEntity event = new BrigadeEventEntity();
        event.setTripProfile(profile);
        event.setStartSecond(startSecond);
        return event;
    }
}
