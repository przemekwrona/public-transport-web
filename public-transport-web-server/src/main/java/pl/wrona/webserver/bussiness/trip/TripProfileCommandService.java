package pl.wrona.webserver.bussiness.trip;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.TripProfileEntity;

@Service
@AllArgsConstructor
public class TripProfileCommandService {

    private final TripProfileCommandRepository tripProfileCommandRepository;

    @Transactional
    public TripProfileEntity save(TripProfileEntity entity) {
        return tripProfileCommandRepository.save(entity);
    }

}
