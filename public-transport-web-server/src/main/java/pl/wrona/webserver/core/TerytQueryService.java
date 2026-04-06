package pl.wrona.webserver.core;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.entity.TerytEntity;
import pl.wrona.webserver.core.entity.TerytRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TerytQueryService {

    private TerytRepository terytRepository;

    public Set<TerytEntity> findTerrytWojewodztwoByWojIn(Set<String> teryts) {
        var wojs = teryts.stream().map(teryt -> teryt.substring(0, 2)).collect(Collectors.toSet());
        return terytRepository.findTerrytWojewodztwoByWojIn(wojs);
    }

    public Set<TerytEntity> findTerrytPowiatByTeryIn(Set<String> teryts) {
        return terytRepository.findTerrytPowiatByTeryIn(teryts);
    }

    public Set<TerytEntity> findTerrytGminaByIdtercIn(Set<String> territoryIdtercs) {
        return terytRepository.findTerrytGminaByIdtercIn(territoryIdtercs);
    }

}
