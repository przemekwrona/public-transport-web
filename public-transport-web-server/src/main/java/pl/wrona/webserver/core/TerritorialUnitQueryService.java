package pl.wrona.webserver.core;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.entity.TerritorialUnitEntity;
import pl.wrona.webserver.core.entity.TerritorialUnitRepository;

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
