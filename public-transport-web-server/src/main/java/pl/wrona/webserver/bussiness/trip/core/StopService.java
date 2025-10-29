package pl.wrona.webserver.bussiness.trip.core;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.trip.core.agency.AgencyEntity;
import pl.wrona.webserver.bussiness.trip.core.entity.StopEntity;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StopService {

    private final StopRepository stopRepository;

    public List<StopEntity> findStopByIdsIn(List<Long> stopIds) {
        return stopRepository.findAllByStopIdIn(stopIds);
    }

    public Map<Long, StopEntity> mapStopByIdsIn(List<Long> stopIds) {
        return findStopByIdsIn(stopIds).stream()
                .collect(Collectors.toMap(StopEntity::getStopId, Function.identity()));
    }

    @Deprecated
    public List<StopEntity> findAllStops(AgencyEntity agencyEntity) {
        return stopRepository.findAllByAgency(agencyEntity);
    }
}
