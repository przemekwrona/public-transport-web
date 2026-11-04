package pl.wrona.webserver.bussiness.stop.rename;

import org.igeolab.iot.pt.server.api.model.AgencyStopNameRequest;
import org.igeolab.iot.pt.server.api.model.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.StopAgencyEntity;
import pl.wrona.webserver.core.agency.StopAgencyHistoryEntity;
import pl.wrona.webserver.core.agency.StopAgencyHistoryRepository;
import pl.wrona.webserver.core.agency.StopAgencyRepository;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgencyStopRenameServiceTest {

    @Mock
    private AgencyService agencyService;
    @Mock
    private StopService stopService;
    @Mock
    private StopAgencyRepository stopAgencyRepository;
    @Mock
    private StopAgencyHistoryRepository stopAgencyHistoryRepository;

    @InjectMocks
    private AgencyStopRenameService agencyStopRenameService;

    @Test
    void storesTheNewNameOnlyForTheAgencyAndArchivesTheCatalogName() {
        AgencyEntity agency = agency(5L);
        StopEntity stop = stop(10L, "Dworzec");
        when(agencyService.findAgencyByAgencyCode("WAWA")).thenReturn(agency);
        when(stopService.findStopByIdsIn(List.of(10L))).thenReturn(List.of(stop));
        when(stopAgencyRepository.findByAgency_AgencyIdAndStop_StopId(5L, 10L)).thenReturn(Optional.empty());

        Status status = agencyStopRenameService.updateAgencyStopName("WAWA", 10L, new AgencyStopNameRequest().name("  Dworzec Główny  "));

        assertThat(status.getStatus()).isEqualTo(Status.StatusEnum.SUCCESS);
        assertThat(stop.getName()).isEqualTo("Dworzec");

        ArgumentCaptor<StopAgencyEntity> current = ArgumentCaptor.forClass(StopAgencyEntity.class);
        verify(stopAgencyRepository).save(current.capture());
        assertThat(current.getValue().getAgency()).isSameAs(agency);
        assertThat(current.getValue().getStop()).isSameAs(stop);
        assertThat(current.getValue().getName()).isEqualTo("Dworzec Główny");
        assertThat(current.getValue().getLon()).isEqualTo(20.0);
        assertThat(current.getValue().getLat()).isEqualTo(50.0);

        ArgumentCaptor<StopAgencyHistoryEntity> history = ArgumentCaptor.forClass(StopAgencyHistoryEntity.class);
        verify(stopAgencyHistoryRepository).save(history.capture());
        assertThat(history.getValue().getAgency()).isSameAs(agency);
        assertThat(history.getValue().getName()).isEqualTo("Dworzec");
        assertThat(history.getValue().getValidTo()).isAfter(history.getValue().getValidFrom());
    }

    @Test
    void archivesThePreviousAgencyNameWhenItChangesAgain() {
        AgencyEntity agency = agency(5L);
        StopEntity stop = stop(10L, "Dworzec");
        LocalDateTime previous = LocalDateTime.of(2026, 1, 1, 8, 0);
        StopAgencyEntity current = new StopAgencyEntity();
        current.setName("Dworzec Główny");
        current.setLon(21.0);
        current.setLat(51.0);
        current.setUpdatedAt(previous);
        when(agencyService.findAgencyByAgencyCode("WAWA")).thenReturn(agency);
        when(stopService.findStopByIdsIn(List.of(10L))).thenReturn(List.of(stop));
        when(stopAgencyRepository.findByAgency_AgencyIdAndStop_StopId(5L, 10L)).thenReturn(Optional.of(current));

        agencyStopRenameService.updateAgencyStopName("WAWA", 10L, new AgencyStopNameRequest().name("Nowa"));

        assertThat(current.getName()).isEqualTo("Nowa");
        assertThat(stop.getName()).isEqualTo("Dworzec");
        ArgumentCaptor<StopAgencyHistoryEntity> history = ArgumentCaptor.forClass(StopAgencyHistoryEntity.class);
        verify(stopAgencyHistoryRepository).save(history.capture());
        assertThat(history.getValue().getName()).isEqualTo("Dworzec Główny");
        assertThat(history.getValue().getLon()).isEqualTo(21.0);
        assertThat(history.getValue().getValidFrom()).isEqualTo(previous);
        assertThat(history.getValue().getValidTo()).isAfter(previous);
        verify(stopAgencyRepository).save(current);
    }

    @Test
    void doesNothingWhenTheAgencyNameIsUnchanged() {
        AgencyEntity agency = agency(5L);
        StopAgencyEntity current = new StopAgencyEntity();
        current.setName("Dworzec Główny");
        when(agencyService.findAgencyByAgencyCode("WAWA")).thenReturn(agency);
        when(stopService.findStopByIdsIn(List.of(10L))).thenReturn(List.of(stop(10L, "Dworzec")));
        when(stopAgencyRepository.findByAgency_AgencyIdAndStop_StopId(5L, 10L)).thenReturn(Optional.of(current));

        agencyStopRenameService.updateAgencyStopName("WAWA", 10L, new AgencyStopNameRequest().name("Dworzec Główny"));

        verify(stopAgencyRepository, never()).save(current);
        verify(stopAgencyHistoryRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsABlankName() {
        assertThatThrownBy(() -> agencyStopRenameService.updateAgencyStopName("WAWA", 10L, new AgencyStopNameRequest().name("  ")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("required");
        verify(stopAgencyRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsAnUnknownStop() {
        when(agencyService.findAgencyByAgencyCode("WAWA")).thenReturn(agency(5L));
        when(stopService.findStopByIdsIn(List.of(10L))).thenReturn(List.of());

        assertThatThrownBy(() -> agencyStopRenameService.updateAgencyStopName("WAWA", 10L, new AgencyStopNameRequest().name("Nowa")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("10");
    }

    private static AgencyEntity agency(Long id) {
        AgencyEntity agency = new AgencyEntity();
        agency.setAgencyId(id);
        agency.setAgencyCode("WAWA");
        return agency;
    }

    private static StopEntity stop(Long id, String name) {
        return StopEntity.builder().stopId(id).name(name).lon(20.0).lat(50.0).build();
    }
}
