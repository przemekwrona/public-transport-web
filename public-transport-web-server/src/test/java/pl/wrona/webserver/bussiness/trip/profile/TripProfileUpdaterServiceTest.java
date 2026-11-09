package pl.wrona.webserver.bussiness.trip.profile;

import org.igeolab.iot.pt.server.api.model.Point2D;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.TrafficMode;
import org.igeolab.iot.pt.server.api.model.TripProfile;
import org.igeolab.iot.pt.server.api.model.UpdateTripProfileRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.bussiness.stoptime.StopTimeCommandService;
import pl.wrona.webserver.bussiness.trip.TripCommandService;
import pl.wrona.webserver.bussiness.trip.TripProfileCommandService;
import pl.wrona.webserver.bussiness.trip.TripProfileQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.agency.TripTrafficMode;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.exception.BusinessException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripProfileUpdaterServiceTest {

    @Mock
    private TripQueryService tripQueryService;
    @Mock
    private StopTimeCommandService stopTimeCommandService;
    @Mock
    private StopService stopService;
    @Mock
    private TripCommandService tripCommandService;
    @Mock
    private TripProfileQueryService tripProfileQueryService;
    @Mock
    private TripProfileCommandService tripProfileCommandService;
    @Mock
    private BrigadeEventQueryService brigadeEventQueryService;

    @InjectMocks
    private TripProfileUpdaterService tripProfileUpdaterService;

    @Test
    void storesCommunicationSpeedDistanceAndStopListOnTheTripProfile() {
        TripEntity trip = new TripEntity();
        trip.setTripId(7L);
        TripProfileEntity profile = new TripProfileEntity();
        profile.setTripProfileId(10L);
        profile.setTrip(trip);

        StopEntity firstStop = new StopEntity();
        firstStop.setStopId(1L);
        StopEntity lastStop = new StopEntity();
        lastStop.setStopId(2L);

        when(tripQueryService.findTripByAgencyAndRouteCodeAndTripCode("NEXTSTOP", "A1", "00A")).thenReturn(trip);
        when(stopService.mapStopByIdsIn(List.of(1L, 2L))).thenReturn(Map.of(1L, firstStop, 2L, lastStop));
        when(tripProfileQueryService.findAllByTripAndTrafficMode(trip, TripTrafficMode.NORMAL)).thenReturn(profile);

        Status status = tripProfileUpdaterService.updateTripProfile("NEXTSTOP", "A1", "00A", request());

        assertThat(status.getStatus()).isEqualTo(Status.StatusEnum.SUCCESS);
        assertThat(profile.getCalculatedCommunicationVelocity()).isEqualTo(40);
        assertThat(profile.getTravelTimeInSeconds()).isEqualTo(600);
        assertThat(trip.getDistanceInMeters()).isEqualTo(10_000);
        assertThat(trip.getTripGeometry().getGeometry()).contains("50.6").contains("21.0");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StopTimeEntity>> stops = ArgumentCaptor.forClass(List.class);
        verify(stopTimeCommandService).deleteAllByTripProfile(profile);
        verify(stopTimeCommandService).saveAll(stops.capture());
        assertThat(stops.getValue()).hasSize(2);
        assertThat(stops.getValue().get(1).getDistanceMeters()).isEqualTo(10_000);
        assertThat(stops.getValue().get(1).getCalculatedTimeSeconds()).isEqualTo(600);
        assertThat(stops.getValue().get(1).getStopEntity()).isSameAs(lastStop);
        verify(tripCommandService).save(trip);
    }

    @Test
    void replacesTheProfileListAndRemovesAProfileThatIsNoLongerPresent() {
        TripEntity trip = new TripEntity();
        trip.setTripId(7L);
        TripProfileEntity normal = profile(10L, trip, TripTrafficMode.NORMAL);
        TripProfileEntity traffic = profile(11L, trip, TripTrafficMode.TRAFFIC);
        StopEntity stop = new StopEntity();
        stop.setStopId(1L);

        when(tripQueryService.findTripByAgencyAndRouteCodeAndTripCode("NEXTSTOP", "A1", "00A")).thenReturn(trip);
        when(stopService.mapStopByIdsIn(List.of(1L, 2L))).thenReturn(Map.of(1L, stop, 2L, stop));
        when(tripProfileQueryService.findAllByTripAndTrafficMode(trip, TripTrafficMode.NORMAL)).thenReturn(normal);
        when(tripProfileQueryService.findAllByTrip(trip)).thenReturn(List.of(normal, traffic));
        when(brigadeEventQueryService.existsByTripProfile(traffic)).thenReturn(false);

        Status status = tripProfileUpdaterService.updateTripProfiles("NEXTSTOP", "A1", "00A", request());

        assertThat(status.getStatus()).isEqualTo(Status.StatusEnum.SUCCESS);
        verify(tripProfileCommandService, never()).delete(normal);
        verify(stopTimeCommandService).deleteAllByTripProfile(traffic);
        verify(tripProfileCommandService).delete(traffic);
    }

    @Test
    void keepsAProfileThatABrigadeStillUses() {
        TripEntity trip = new TripEntity();
        TripProfileEntity traffic = profile(11L, trip, TripTrafficMode.TRAFFIC);
        when(tripQueryService.findTripByAgencyAndRouteCodeAndTripCode("NEXTSTOP", "A1", "00A")).thenReturn(trip);
        when(stopService.mapStopByIdsIn(List.of(1L, 2L))).thenReturn(Map.of());
        when(tripProfileQueryService.findAllByTripAndTrafficMode(trip, TripTrafficMode.NORMAL)).thenReturn(profile(10L, trip, TripTrafficMode.NORMAL));
        when(tripProfileQueryService.findAllByTrip(trip)).thenReturn(List.of(traffic));
        when(brigadeEventQueryService.existsByTripProfile(traffic)).thenReturn(true);

        assertThatThrownBy(() -> tripProfileUpdaterService.updateTripProfiles("NEXTSTOP", "A1", "00A", request()))
                .isInstanceOf(BusinessException.class);
        verify(tripProfileCommandService, never()).delete(traffic);
    }

    private static TripProfileEntity profile(long id, TripEntity trip, TripTrafficMode trafficMode) {
        TripProfileEntity profile = new TripProfileEntity();
        profile.setTripProfileId(id);
        profile.setTrip(trip);
        profile.setTrafficMode(trafficMode);
        return profile;
    }

    @Test
    void rejectsAMissingTrip() {
        when(tripQueryService.findTripByAgencyAndRouteCodeAndTripCode("NEXTSTOP", "A1", "00A")).thenReturn(null);

        assertThatThrownBy(() -> tripProfileUpdaterService.updateTripProfile("NEXTSTOP", "A1", "00A", request()))
                .isInstanceOf(BusinessException.class);
    }

    private static UpdateTripProfileRequest request() {
        return new UpdateTripProfileRequest()
                .tripProfiles(List.of(new TripProfile()
                        .trafficMode(TrafficMode.NORMAL)
                        .isDefault(true)
                        .isCustomized(false)
                        .calculatedCommunicationVelocity(40)
                        .addStopsItem(new StopTime()
                                .stopId(1L)
                                .meters(0)
                                .calculatedSeconds(0)
                                .customizedSeconds(0))
                        .addStopsItem(new StopTime()
                                .stopId(2L)
                                .meters(10_000)
                                .calculatedSeconds(600)
                                .customizedSeconds(600))))
                .geometry(List.of(new Point2D().lat(50.6f).lon(21.0f)));
    }
}
