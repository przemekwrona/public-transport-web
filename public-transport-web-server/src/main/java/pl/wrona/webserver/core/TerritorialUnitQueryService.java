package pl.wrona.webserver.core;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.entity.TerritorialUnitEntity;
import pl.wrona.webserver.core.entity.TerritorialUnitRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class TerritorialUnitQueryService {

    private final TerritorialUnitRepository territorialUnitRepository;

    public List<TerritorialUnitEntity> findAllByStopIn(List<Long> territoriesIds) {
        return territorialUnitRepository.findAllByStopIn(territoriesIds);
    }
}
