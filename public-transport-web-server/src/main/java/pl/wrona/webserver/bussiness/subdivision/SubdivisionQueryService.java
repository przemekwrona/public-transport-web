package pl.wrona.webserver.bussiness.subdivision;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Subdivision;
import org.igeolab.iot.pt.server.api.model.SubdivisionByStopsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.stop.TerritorialUnitQueryService;
import pl.wrona.webserver.bussiness.stop.TerytQueryService;
import pl.wrona.webserver.core.stop.SubdivisionEntity;
import pl.wrona.webserver.core.stop.SubdivisionRepository;
import pl.wrona.webserver.core.stop.TerritorialUnitEntity;
import pl.wrona.webserver.core.stop.TerytEntity;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SubdivisionQueryService {

    private final TerritorialUnitQueryService territorialUnitQueryService;
    private final TerytQueryService terytQueryService;
    private final SubdivisionRepository subdivisionRepository;

    @Transactional(readOnly = true)
    public SubdivisionByStopsResponse findSubdivisionsByStopIds(List<Long> stopIds) {
        if (stopIds == null || stopIds.isEmpty()) {
            return new SubdivisionByStopsResponse().subdivisions(List.of());
        }

        Map<String, TerritorialUnitEntity> unitsByIdterc = territorialUnitQueryService.findAllByStopIdIn(stopIds).stream()
                .filter(unit -> hasText(unit.getIdterc()))
                .collect(Collectors.toMap(
                        TerritorialUnitEntity::getIdterc,
                        Function.identity(),
                        (left, right) -> left));

        if (unitsByIdterc.isEmpty()) {
            return new SubdivisionByStopsResponse().subdivisions(List.of());
        }

        Set<String> idtercs = unitsByIdterc.keySet();
        Set<String> powiatTeryts = unitsByIdterc.values().stream()
                .map(TerritorialUnitEntity::getTeryt)
                .filter(this::hasText)
                .collect(Collectors.toSet());

        Map<String, SubdivisionEntity> geometriesByTeryt = indexByTeryt(
                subdivisionRepository.findAllByTerytIn(idtercs));
        Map<String, String> gminaNamesByIdterc = terytQueryService.findGminaNamesByIdtercIn(idtercs);
        Map<String, TerytEntity> powiatyByTeryt = indexByTerytCode(
                terytQueryService.findTerrytPowiatByTeryIn(powiatTeryts));
        Map<String, TerytEntity> wojewodztwaByTeryt = indexByTerytCode(
                terytQueryService.findTerrytWojewodztwoByWojIn(powiatTeryts));

        List<Subdivision> subdivisions = unitsByIdterc.entrySet().stream()
                .map(entry -> toSubdivision(
                        entry.getKey(),
                        entry.getValue(),
                        geometriesByTeryt,
                        gminaNamesByIdterc,
                        powiatyByTeryt,
                        wojewodztwaByTeryt))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Subdivision::getTeryt))
                .toList();

        return new SubdivisionByStopsResponse().subdivisions(subdivisions);
    }

    private Subdivision toSubdivision(String idterc,
                                      TerritorialUnitEntity unit,
                                      Map<String, SubdivisionEntity> geometriesByTeryt,
                                      Map<String, String> gminaNamesByIdterc,
                                      Map<String, TerytEntity> powiatyByTeryt,
                                      Map<String, TerytEntity> wojewodztwaByTeryt) {
        SubdivisionEntity geometry = geometriesByTeryt.get(idterc);
        if (geometry == null) {
            return null;
        }

        String powiatTeryt = unit.getTeryt();
        String wojewodztwoTeryt = hasText(powiatTeryt) && powiatTeryt.length() >= 2
                ? powiatTeryt.substring(0, 2)
                : null;

        TerytEntity powiat = hasText(powiatTeryt) ? powiatyByTeryt.get(powiatTeryt) : null;
        TerytEntity wojewodztwo = hasText(wojewodztwoTeryt) ? wojewodztwaByTeryt.get(wojewodztwoTeryt) : null;

        return new Subdivision()
                .teryt(idterc)
                .wojewodztwo(nameOf(wojewodztwo))
                .powiat(nameOf(powiat))
                .gmina(gminaNamesByIdterc.get(idterc))
                .geometry(geometry.getGeometry());
    }

    private Map<String, SubdivisionEntity> indexByTeryt(Collection<SubdivisionEntity> subdivisions) {
        return subdivisions.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(SubdivisionEntity::getTeryt, Function.identity(), (left, right) -> left));
    }

    private Map<String, TerytEntity> indexByTerytCode(Collection<TerytEntity> entities) {
        return entities.stream()
                .filter(Objects::nonNull)
                .filter(entity -> hasText(entity.getTeryt()))
                .collect(Collectors.toMap(TerytEntity::getTeryt, Function.identity(), (left, right) -> left));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String nameOf(TerytEntity entity) {
        return entity == null ? null : entity.getNazwa();
    }
}
