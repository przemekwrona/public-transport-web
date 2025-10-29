package pl.wrona.webserver.bussiness.trip;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.TripEntity;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TripQueryService {

    private final TripQueryRepository tripQueryRepository;

    public List<TripEntity> findByAgencyCodeAndLineAndName(String agencyCode, String line, String name) {
        return tripQueryRepository.findByAgencyCodeAndLineAndName(agencyCode, line, name);
    }

    public List<TripEntity> findByExistsBrigade(String agency) {
        return tripQueryRepository.findByExistsBrigade(agency);
    }

    public Map<Long, TripEntity> mapByExistsBrigade(String agency) {
        return findByExistsBrigade(agency).stream()
                .collect(Collectors.toMap(TripEntity::getTripId, Function.identity()));
    }
}
