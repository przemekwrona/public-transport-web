package pl.wrona.webserver.bussiness.stoptime;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.StopTimeEntity;

import java.util.List;

@Service
@AllArgsConstructor
public class StopTimeCommandService {

    private final StopTimeCommandRepository stopTimeCommandRepository;

    public void deleteByTripId(Long tripId) {
        stopTimeCommandRepository.deleteByTripId(tripId);
    }


    public void saveAll(List<StopTimeEntity> entities) {
        stopTimeCommandRepository.saveAll(entities);
    }

}
