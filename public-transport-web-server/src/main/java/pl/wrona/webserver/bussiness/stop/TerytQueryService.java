package pl.wrona.webserver.bussiness.stop;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.stop.TerytEntity;
import pl.wrona.webserver.core.stop.TerytNameView;
import pl.wrona.webserver.core.stop.TerytRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
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

    public Map<String, String> findGminaNamesByIdtercIn(Collection<String> idtercs) {
        if (idtercs == null || idtercs.isEmpty()) {
            return Map.of();
        }

        Set<String> requested = idtercs.stream()
                .filter(Objects::nonNull)
                .filter(idterc -> !idterc.isBlank())
                .collect(Collectors.toCollection(HashSet::new));
        if (requested.isEmpty()) {
            return Map.of();
        }

        Set<String> prefixes = requested.stream()
                .filter(idterc -> idterc.length() >= 6)
                .map(idterc -> idterc.substring(0, 6))
                .collect(Collectors.toCollection(HashSet::new));

        Map<String, String> byIdterc = new HashMap<>();
        Map<String, String> byPrefix = new HashMap<>();
        for (TerytNameView view : terytRepository.findNamesByIdtercInOrPrefixIn(requested, prefixes)) {
            if (view == null || view.getIdterc() == null || view.getIdterc().isBlank() || view.getNazwa() == null) {
                continue;
            }
            byIdterc.putIfAbsent(view.getIdterc(), view.getNazwa());
            if (view.getIdterc().length() >= 6) {
                byPrefix.putIfAbsent(view.getIdterc().substring(0, 6), view.getNazwa());
            }
        }

        Map<String, String> names = new HashMap<>();
        for (String idterc : requested) {
            String name = byIdterc.get(idterc);
            if (name == null && idterc.length() >= 6) {
                name = byPrefix.get(idterc.substring(0, 6));
            }
            if (name != null && !name.isBlank()) {
                names.put(idterc, name);
            }
        }
        return names;
    }

}
