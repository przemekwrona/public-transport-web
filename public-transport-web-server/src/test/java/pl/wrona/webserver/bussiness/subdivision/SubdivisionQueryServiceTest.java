package pl.wrona.webserver.bussiness.subdivision;

import org.igeolab.iot.pt.server.api.model.Subdivision;
import org.igeolab.iot.pt.server.api.model.SubdivisionByStopsResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrona.webserver.bussiness.stop.TerritorialUnitQueryService;
import pl.wrona.webserver.bussiness.stop.TerytQueryService;
import pl.wrona.webserver.core.stop.SubdivisionEntity;
import pl.wrona.webserver.core.stop.SubdivisionRepository;
import pl.wrona.webserver.core.stop.TerritorialUnitEntity;
import pl.wrona.webserver.core.stop.TerytEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubdivisionQueryServiceTest {

    @Mock
    private TerritorialUnitQueryService territorialUnitQueryService;
    @Mock
    private TerytQueryService terytQueryService;
    @Mock
    private SubdivisionRepository subdivisionRepository;

    @InjectMocks
    private SubdivisionQueryService subdivisionQueryService;

    @Test
    void returnsEmptyResponseForEmptyStopIds() {
        SubdivisionByStopsResponse response = subdivisionQueryService.findSubdivisionsByStopIds(List.of());

        assertThat(response.getSubdivisions()).isEmpty();
        verify(territorialUnitQueryService, never()).findAllByStopIdIn(anyList());
    }

    @Test
    void mapsUniqueGminaSubdivisionsWithRegionNames() {
        when(territorialUnitQueryService.findAllByStopIdIn(List.of(1L, 2L, 3L))).thenReturn(List.of(
                unit("0264011", "0264"),
                unit("0264011", "0264"),
                unit("0264022", "0264")
        ));
        when(subdivisionRepository.findAllByTerytIn(Set.of("0264011", "0264022"))).thenReturn(List.of(
                geometry("0264011", "POLYGON ((1 1, 2 2, 1 1))"),
                geometry("0264022", "POLYGON ((3 3, 4 4, 3 3))")
        ));
        when(terytQueryService.findGminaNamesByIdtercIn(Set.of("0264011", "0264022"))).thenReturn(Map.of(
                "0264011", "Wrocław",
                "0264022", "Żerniki"
        ));
        when(terytQueryService.findTerrytPowiatByTeryIn(Set.of("0264"))).thenReturn(Set.of(
                teryt("0264", "0264", "02", "powiat Wrocław")
        ));
        when(terytQueryService.findTerrytWojewodztwoByWojIn(Set.of("0264"))).thenReturn(Set.of(
                teryt("02", "02", "02", "DOLNOŚLĄSKIE")
        ));

        SubdivisionByStopsResponse response = subdivisionQueryService.findSubdivisionsByStopIds(List.of(1L, 2L, 3L));

        assertThat(response.getSubdivisions()).hasSize(2);
        assertThat(response.getSubdivisions())
                .extracting(Subdivision::getTeryt)
                .containsExactly("0264011", "0264022");
        assertThat(response.getSubdivisions().get(0))
                .returns("DOLNOŚLĄSKIE", Subdivision::getWojewodztwo)
                .returns("powiat Wrocław", Subdivision::getPowiat)
                .returns("Wrocław", Subdivision::getGmina)
                .returns("POLYGON ((1 1, 2 2, 1 1))", Subdivision::getGeometry);
    }

    @Test
    void skipsUnitsWithoutSubdivisionGeometry() {
        when(territorialUnitQueryService.findAllByStopIdIn(List.of(10L))).thenReturn(List.of(
                unit("9999999", "9999")
        ));
        when(subdivisionRepository.findAllByTerytIn(Set.of("9999999"))).thenReturn(List.of());
        when(terytQueryService.findGminaNamesByIdtercIn(Set.of("9999999"))).thenReturn(Map.of());
        when(terytQueryService.findTerrytPowiatByTeryIn(Set.of("9999"))).thenReturn(Set.of());
        when(terytQueryService.findTerrytWojewodztwoByWojIn(Set.of("9999"))).thenReturn(Set.of());

        SubdivisionByStopsResponse response = subdivisionQueryService.findSubdivisionsByStopIds(List.of(10L));

        assertThat(response.getSubdivisions()).isEmpty();
        verify(subdivisionRepository).findAllByTerytIn(anyCollection());
        verify(terytQueryService).findGminaNamesByIdtercIn(anySet());
    }

    private static TerritorialUnitEntity unit(String idterc, String teryt) {
        return TerritorialUnitEntity.builder()
                .idterc(idterc)
                .teryt(teryt)
                .build();
    }

    private static SubdivisionEntity geometry(String teryt, String geometry) {
        return SubdivisionEntity.builder()
                .teryt(teryt)
                .geometry(geometry)
                .build();
    }

    private static TerytEntity teryt(String teryt, String idterc, String woj, String nazwa) {
        return TerytEntity.builder()
                .teryt(teryt)
                .idterc(idterc)
                .woj(woj)
                .nazwa(nazwa)
                .build();
    }
}
