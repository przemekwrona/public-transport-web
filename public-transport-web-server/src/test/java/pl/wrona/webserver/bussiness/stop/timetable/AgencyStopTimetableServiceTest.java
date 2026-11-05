package pl.wrona.webserver.bussiness.stop.timetable;

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
import pl.wrona.webserver.bussiness.stop.name.AgencyStopNameService;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.TerritorialUnitQueryService;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.core.entity.TerritorialUnitEntity;
import pl.wrona.webserver.exception.BusinessException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgencyStopTimetableServiceTest {

    @Mock
    private StopTimeRepository stopTimeRepository;
    @Mock
    private BrigadeEventQueryService brigadeEventQueryService;
    @Mock
    private StopService stopService;
    @Mock
    private AgencyRepository agencyRepository;
    @Mock
    private TerritorialUnitQueryService territorialUnitQueryService;
    @Mock
    private AgencyStopNameService agencyStopNameService;
    @Mock
    private AgencyStopTimetablePdfRenderer agencyStopTimetablePdfRenderer;

    @InjectMocks
    private AgencyStopTimetableService agencyStopTimetableService;

    @Test
    void buildsAlphabeticalDirectionsWithInlineTimesAndLegend() {
        stubStop("WAWA", 10L, "Kielce, Dworzec");
        TripEntity krakow = trip(1L, "Dworzec", "S", "Skrócony");
        TripEntity lodz = trip(2L, "Łódź", "", "");
        TripEntity checiny = trip(3L, "", "", "");
        TripProfileEntity krakowProfile = profile(11L, krakow);
        TripProfileEntity lodzProfile = profile(12L, lodz);
        TripProfileEntity checinyProfile = profile(13L, checiny);

        StopTimeEntity krakowVisit = visit(krakowProfile, 1, 300, stop(10L, "Kielce, Dworzec", null));
        StopTimeEntity krakowEnd = visit(krakowProfile, 4, 2000, stop(20L, "Rynek", 5L));
        StopTimeEntity lodzVisit = visit(lodzProfile, 1, 0, stop(10L, "Kielce, Dworzec", null));
        StopTimeEntity lodzEnd = visit(lodzProfile, 3, 0, stop(21L, "Łódź Fabryczna", 6L));
        StopTimeEntity checinyVisit = visit(checinyProfile, 2, 600, stop(10L, "Kielce, Dworzec", null));
        StopTimeEntity checinyEnd = visit(checinyProfile, 5, 3000, stop(22L, "Chęciny Rynek", 7L));
        StopTimeEntity repeated = visit(krakowProfile, 1, 300, stop(10L, "Kielce, Dworzec", null));

        when(stopTimeRepository.findAllByAgencyCodeAndStopId("WAWA", 10L))
                .thenReturn(List.of(krakowVisit, lodzVisit, checinyVisit, repeated));
        when(stopTimeRepository.findAllByTripIdIn(anyCollection()))
                .thenReturn(List.of(krakowVisit, krakowEnd, lodzVisit, lodzEnd, checinyVisit, checinyEnd));
        when(brigadeEventQueryService.findAllByTripIds(anyCollection())).thenReturn(List.of(
                event(krakowProfile, 6 * 3600, calendar("D", "Dni robocze")),
                event(krakowProfile, 6 * 3600, calendar("D", "Dni robocze")),
                event(lodzProfile, 15 * 3600, calendar("", "")),
                event(lodzProfile, 6 * 3600 + 30 * 60, calendar(null, null)),
                event(checinyProfile, 5 * 3600, calendar("B", "Sobota"))));
        when(territorialUnitQueryService.findAllByTerritoriesIdIn(any())).thenReturn(List.of(
                territory(5L, "Kraków"),
                territory(6L, "Łódź"),
                territory(7L, "Chęciny")));
        when(agencyStopNameService.findOverrides(eq("WAWA"), any())).thenReturn(Map.of());
        when(agencyStopTimetablePdfRenderer.render(any())).thenReturn("%PDF-stop".getBytes());

        ResponseEntity<Resource> response = agencyStopTimetableService.downloadAgencyStopTimetable("WAWA", 10L);

        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment;")
                .contains("rozklad-przystanek-10.pdf");
        AgencyStopTimetableView view = capturedView();
        assertThat(view.agencyName()).isEqualTo("Następna Stacja");
        assertThat(view.stopName()).isEqualTo("Kielce, Dworzec");
        assertThat(view.directions()).extracting(AgencyStopTimetableView.DirectionRow::direction)
                .containsExactly("Chęciny Rynek / Chęciny", "Dworzec / Kraków", "Łódź");
        assertThat(view.directions().get(0).departures()).containsExactly(
                new AgencyStopTimetableView.DepartureTime("05:10", List.of("B")));
        assertThat(view.directions().get(1).departures()).containsExactly(
                new AgencyStopTimetableView.DepartureTime("06:05", List.of("D", "S")));
        assertThat(view.directions().get(2).departures()).extracting(AgencyStopTimetableView.DepartureTime::time)
                .containsExactly("06:30", "15:00");
        assertThat(view.legend()).containsExactly(
                new AgencyStopTimetableView.LegendEntry("B", "Sobota"),
                new AgencyStopTimetableView.LegendEntry("D", "Dni robocze"),
                new AgencyStopTimetableView.LegendEntry("S", "Skrócony"));
    }

    @Test
    void skipsDeparturesFromTheFinalStop() {
        stubStop("WAWA", 10L, "Pętla");
        TripEntity trip = trip(1L, "Centrum", "A", "Wariant");
        TripProfileEntity profile = profile(11L, trip);
        StopTimeEntity terminus = visit(profile, 4, 100, stop(10L, "Pętla", 1L));
        when(stopTimeRepository.findAllByAgencyCodeAndStopId("WAWA", 10L)).thenReturn(List.of(terminus));
        when(stopTimeRepository.findAllByTripIdIn(List.of(1L))).thenReturn(List.of(terminus));
        when(brigadeEventQueryService.findAllByTripIds(List.of(1L))).thenReturn(List.of(event(profile, 8 * 3600, calendar("D", "Dni robocze"))));
        when(territorialUnitQueryService.findAllByTerritoriesIdIn(any())).thenReturn(List.of(territory(1L, "Kielce")));
        when(agencyStopNameService.findOverrides(eq("WAWA"), any())).thenReturn(Map.of());
        when(agencyStopTimetablePdfRenderer.render(any())).thenReturn("%PDF".getBytes());

        agencyStopTimetableService.downloadAgencyStopTimetable("WAWA", 10L);

        AgencyStopTimetableView view = capturedView();
        assertThat(view.directions()).isEmpty();
        assertThat(view.legend()).isEmpty();
    }

    @Test
    void rejectsUnknownStop() {
        when(stopService.mapStopByIdsIn(List.of(99L))).thenReturn(Map.of());

        assertThatThrownBy(() -> agencyStopTimetableService.downloadAgencyStopTimetable("WAWA", 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("99");
    }

    private AgencyStopTimetableView capturedView() {
        ArgumentCaptor<AgencyStopTimetableView> captor = ArgumentCaptor.forClass(AgencyStopTimetableView.class);
        verify(agencyStopTimetablePdfRenderer).render(captor.capture());
        return captor.getValue();
    }

    private void stubStop(String agencyCode, long stopId, String stopName) {
        AgencyEntity agency = new AgencyEntity();
        agency.setAgencyName("Następna Stacja");
        when(agencyRepository.findByAgencyCodeEquals(agencyCode)).thenReturn(agency);
        when(stopService.mapStopByIdsIn(List.of(stopId))).thenReturn(Map.of(stopId, stop(stopId, stopName, null)));
    }

    private static TripEntity trip(Long tripId, String headsign, String designation, String description) {
        return TripEntity.builder()
                .tripId(tripId)
                .headsign(headsign)
                .variantDesignation(designation)
                .variantDescription(description)
                .build();
    }

    private static TripProfileEntity profile(Long profileId, TripEntity trip) {
        return TripProfileEntity.builder()
                .tripProfileId(profileId)
                .trip(trip)
                .build();
    }

    private static StopEntity stop(Long stopId, String name, Long territorialUnitId) {
        return StopEntity.builder()
                .stopId(stopId)
                .name(name)
                .territorialUnitId(territorialUnitId)
                .build();
    }

    private static StopTimeEntity visit(TripProfileEntity profile, int sequence, int customizedSeconds, StopEntity stop) {
        return StopTimeEntity.builder()
                .stopTimeId(new StopTimeId(profile.getTripProfileId(), sequence))
                .tripProfile(profile)
                .stopEntity(stop)
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

    private static TerritorialUnitEntity territory(Long id, String name) {
        return TerritorialUnitEntity.builder()
                .territorialUnitId(id)
                .nazwa(name)
                .build();
    }
}
