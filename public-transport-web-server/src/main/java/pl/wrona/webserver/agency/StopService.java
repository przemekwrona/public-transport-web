package pl.wrona.webserver.agency;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.agency.entity.Agency;
import pl.wrona.webserver.agency.entity.StopEntity;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StopService {

    private final StopRepository stopRepository;

    public List<StopEntity> findStopByIdsIn(List<String> stopIds) {
        return stopRepository.findAllByStopIdIn(stopIds);
    }

    public Map<String, StopEntity> mapStopByIdsIn(List<String> stopIds) {
        return findStopByIdsIn(stopIds).stream()
                .collect(Collectors.toMap(StopEntity::getStopId, Function.identity()));
    }

    public List<StopEntity> findAllStops(Agency agency) {
        return stopRepository.findAllByAgency(agency);
    }
}
