package pl.wrona.webserver.bussiness.route.stop.timetable;

import org.igeolab.iot.pt.server.api.model.RouteStopTimetable;
import org.igeolab.iot.pt.server.api.model.TripMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.exception.BusinessException;

import java.util.List;
import java.util.Map;

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
    @Mock
    private RouteQueryService routeQueryService;
    @Mock
    private StopService stopService;
    @Mock
    private AgencyRepository agencyRepository;
    @Mock
    private RouteStopTimetablePdfRenderer routeStopTimetablePdfRenderer;

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
        verify(routeStopTimetablePdfRenderer, never()).render(any());
    }

    @Test
    void returnsDeparturesAtStopForTripsInRequestedMode() {
        TripEntity trip = trip(1L, "T1", "S", "Skrócony");
        TripProfileEntity profile = profile(11L, trip);
        StopTimeEntity stopTime = stopTime(profile, 1, 300);
        BrigadeEventEntity event = event(profile, 6 * 3600, calendar("D", "Dni robocze"));

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
        verify(routeStopTimetablePdfRenderer, never()).render(any());
    }

    @Test
    void skipsEventsWhoseProfileDoesNotServeTheStop() {
        TripEntity trip = trip(1L, "T1", null, null);
        TripProfileEntity served = profile(11L, trip);
        TripProfileEntity other = profile(12L, trip);

        when(tripQueryService.findByAgencyAndRouteCodeAndVariantMode("WAWA", "R1", TripVariantMode.FRONT))
                .thenReturn(List.of(trip));
        when(stopTimeRepository.findAllByTripIdInAndStopId(List.of(1L), 10L)).thenReturn(List.of(stopTime(served, 1, 0)));
        when(brigadeEventQueryService.findAllByTripIds(List.of(1L))).thenReturn(List.of(event(other, 7 * 3600, calendar("D", "Dni robocze"))));

        RouteStopTimetable response = routeStopTimetableService.getRouteStopTimetable("WAWA", "R1", "10", TripMode.FRONT);

        assertThat(response.getDepartures()).isEmpty();
    }

    @Test
    void returnsPdfWhenRequested() {
        stubHeader("WAWA", "R1", 10L, "Dworzec");
        TripEntity trip = trip(1L, "T1", "S", "Skrócony");
        TripProfileEntity profile = profile(11L, trip);
        when(tripQueryService.findByAgencyAndRouteCodeAndVariantMode("WAWA", "R1", TripVariantMode.BACK))
                .thenReturn(List.of(trip));
        when(stopTimeRepository.findAllByTripIdInAndStopId(List.of(1L), 10L)).thenReturn(List.of(stopTime(profile, 1, 300)));
        when(brigadeEventQueryService.findAllByTripIds(List.of(1L))).thenReturn(List.of(event(profile, 6 * 3600, calendar("D", "Dni robocze"))));
        when(routeStopTimetablePdfRenderer.render(any())).thenReturn("%PDF-departures".getBytes());

        ResponseEntity<Resource> response = routeStopTimetableService.getRouteStopTimetablePdf("WAWA", "R1", "10", TripMode.BACK);

        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("timetable-R1-10-BACK.pdf");
        RouteStopTimetableView view = capturedView();
        assertThat(view.tripMode()).isEqualTo("BACK");
        assertThat(view.calendars()).hasSize(1);
        assertThat(view.calendars().get(0).hours().get(0).minutes().get(0).minute()).isEqualTo("05");
        assertThat(view.legend()).containsExactly(new RouteStopTimetableView.LegendEntry("S", "Skrócony"));
    }

    @Test
    void rejectsNonNumericStopCode() {
        assertThatThrownBy(() -> routeStopTimetableService.getRouteStopTimetable("WAWA", "R1", "ABC", TripMode.FRONT))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ABC");
    }

    @Test
    void prefersPdfOnlyWhenAcceptAsksForPdf() {
        assertThat(RouteStopTimetableService.wantsPdf(null)).isFalse();
        assertThat(RouteStopTimetableService.wantsPdf("application/json")).isFalse();
        assertThat(RouteStopTimetableService.wantsPdf("*/*")).isFalse();
        assertThat(RouteStopTimetableService.wantsPdf("application/pdf")).isTrue();
        assertThat(RouteStopTimetableService.wantsPdf("application/pdf, application/json")).isTrue();
        assertThat(RouteStopTimetableService.wantsPdf("application/json, application/pdf")).isFalse();
    }

    private RouteStopTimetableView capturedView() {
        ArgumentCaptor<RouteStopTimetableView> captor = ArgumentCaptor.forClass(RouteStopTimetableView.class);
        verify(routeStopTimetablePdfRenderer).render(captor.capture());
        return captor.getValue();
    }

    private void stubHeader(String agencyCode, String routeCode, long stopId, String stopName) {
        AgencyEntity agency = new AgencyEntity();
        agency.setAgencyName("Następna Stacja");
        RouteEntity route = new RouteEntity();
        route.setLine("L1");
        route.setName("Kielce - Kraków");
        route.setDestinationStopName("Kraków");
        route.setOriginStopName("Kielce");
        when(agencyRepository.findByAgencyCodeEquals(agencyCode)).thenReturn(agency);
        when(routeQueryService.findRouteByAgencyCodeAndRouteCode(agencyCode, routeCode)).thenReturn(route);
        when(stopService.mapStopByIdsIn(List.of(stopId))).thenReturn(Map.of(stopId, StopEntity.builder().stopId(stopId).name(stopName).build()));
    }

    private static TripEntity trip(Long tripId, String tripCode, String designation, String description) {
        return TripEntity.builder()
                .tripId(tripId)
                .tripCode(tripCode)
                .variantDesignation(designation)
                .variantDescription(description)
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

    private static BrigadeEventEntity event(TripProfileEntity profile, int startSecond, CalendarSymbolEntity calendar) {
        BrigadeGroupEntity group = new BrigadeGroupEntity();
        group.setCalendarSymbol(calendar);
        BrigadeResourceEntity resource = new BrigadeResourceEntity();
        resource.setBrigadeGroup(group);
        BrigadeEventEntity event = new BrigadeEventEntity();
        event.setTripProfile(profile);
        event.setStartSecond(startSecond);
        event.setResource(resource);
        return event;
    }

    private static CalendarSymbolEntity calendar(String designation, String description) {
        CalendarSymbolEntity calendar = new CalendarSymbolEntity();
        calendar.setDesignation(designation);
        calendar.setDescription(description);
        return calendar;
    }
}
