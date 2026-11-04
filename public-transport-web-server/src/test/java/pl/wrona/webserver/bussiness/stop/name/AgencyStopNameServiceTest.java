package pl.wrona.webserver.bussiness.stop.name;

import org.igeolab.iot.pt.server.api.model.Stop;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.StopAgencyEntity;
import pl.wrona.webserver.core.agency.StopAgencyRepository;
import pl.wrona.webserver.core.entity.StopEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgencyStopNameServiceTest {

    @Mock
    private AgencyRepository agencyRepository;
    @Mock
    private StopAgencyRepository stopAgencyRepository;

    @InjectMocks
    private AgencyStopNameService agencyStopNameService;

    @Test
    void returnsTheAgencyNameAheadOfTheCatalogName() {
        AgencyEntity agency = new AgencyEntity();
        agency.setAgencyId(5L);
        StopEntity stop = StopEntity.builder().stopId(10L).build();
        StopAgencyEntity override = new StopAgencyEntity();
        override.setStop(stop);
        override.setName("Rynek Miejski");
        when(agencyRepository.findByAgencyCodeEquals("WAWA")).thenReturn(agency);
        when(stopAgencyRepository.findByAgencyIdAndStopIds(5L, List.of(10L, 20L))).thenReturn(List.of(override));

        Map<Long, String> names = agencyStopNameService.findOverrides("WAWA", List.of(10L, 20L));

        assertThat(AgencyStopNameService.prefer("Rynek", names, 10L)).isEqualTo("Rynek Miejski");
        assertThat(AgencyStopNameService.prefer("Dworzec", names, 20L)).isEqualTo("Dworzec");
    }

    @Test
    void appliesAgencyNamesOntoReturnedStops() {
        Stop overridden = new Stop().id(10L).name("Rynek");
        Stop catalog = new Stop().id(20L).name("Dworzec");

        agencyStopNameService.apply(Map.of(10L, "Rynek Miejski"), overridden, catalog);

        assertThat(overridden.getName()).isEqualTo("Rynek Miejski");
        assertThat(catalog.getName()).isEqualTo("Dworzec");
    }
}
