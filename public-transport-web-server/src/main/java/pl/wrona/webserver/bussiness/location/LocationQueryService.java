package pl.wrona.webserver.bussiness.location;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.LocationSearch;
import org.igeolab.iot.pt.server.api.model.LocationSearchResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.TerytQueryService;
import pl.wrona.webserver.core.entity.TerritorialUnitEntity;
import pl.wrona.webserver.core.entity.TerritorialUnitRepository;
import pl.wrona.webserver.core.entity.TerytEntity;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LocationQueryService {

    private TerritorialUnitRepository territorialUnitRepository;
    private TerytQueryService terytQueryService;

    public LocationSearchResponse findLocationByName(String q) {
        var locations = territorialUnitRepository.findAllByNazwaStartingWith(q.toUpperCase());

        var teryts = locations.stream().map(TerritorialUnitEntity::getTeryt).collect(Collectors.toSet());

        var voivodship = terytQueryService.findTerrytWojewodztwoByWojIn(teryts).stream().collect(Collectors.toMap(TerytEntity::getTeryt, Function.identity()));
        var powiat = terytQueryService.findTerrytPowiatByTeryIn(teryts).stream().collect(Collectors.toMap(TerytEntity::getTeryt, Function.identity()));

        var locationSearchResponse = locations.stream()
                .sorted(Comparator.comparing(TerritorialUnitEntity::getLiczMiesz, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(it -> new LocationSearch()
                        .territorialUnitId(it.getTerritorialUnitId().toString())
                        .name(it.getNazwa())
                        .woj(voivodship.get(it.getTeryt().substring(0, 2)).getNazwa())
                        .pow(powiat.get(it.getTeryt()).getNazwa())
                        .gm(String.valueOf(it.getLiczMiesz())))
                .toList();

        return new LocationSearchResponse()
                .locations(locationSearchResponse);
    }
}
