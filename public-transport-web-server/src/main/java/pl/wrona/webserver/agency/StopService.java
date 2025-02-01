package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.agency.entity.Stop;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StopService {

    private final StopRepository stopRepository;

    public List<Stop> findStopByIdsIn(List<String> stopIds) {
        return stopRepository.findAllByStopIdIn(stopIds);
    }

    public Map<String, Stop> mapStopByIdsIn(List<String> stopIds) {
        return findStopByIdsIn(stopIds).stream()
                .collect(Collectors.toMap(Stop::getStopId, Function.identity()));
    }
}
