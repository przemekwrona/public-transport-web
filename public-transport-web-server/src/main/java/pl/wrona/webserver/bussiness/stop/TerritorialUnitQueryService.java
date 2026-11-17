package pl.wrona.webserver.bussiness.stop;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.stop.TerritorialUnitEntity;
import pl.wrona.webserver.core.stop.TerritorialUnitRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class TerritorialUnitQueryService {

    private final TerritorialUnitRepository territorialUnitRepository;

    public List<TerritorialUnitEntity> findAllByTerritoriesIdIn(List<Long> territoriesIds) {
        return territorialUnitRepository.findAllByTerritoriesIdIn(territoriesIds);
    }

    public List<TerritorialUnitEntity> findAllByStopIdIn(List<Long> stopIds) {
        return territorialUnitRepository.findAllByStopIdIn(stopIds);
    }

}
