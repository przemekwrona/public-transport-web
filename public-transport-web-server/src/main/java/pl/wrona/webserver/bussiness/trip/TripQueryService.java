package pl.wrona.webserver.bussiness.trip;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripTrafficMode;
import pl.wrona.webserver.core.agency.TripVariantMode;

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

    public boolean existsUniqueTripIndex(String agencyCode, String line, String name, TripVariantMode tripMode, TripTrafficMode trafficMode) {
        return tripQueryRepository.existsTripUniqueIndex(agencyCode, line, name, tripMode, trafficMode);
    }

    public List<TripEntity> findByExistsBrigade(String agency) {
        return tripQueryRepository.findByExistsBrigade(agency);
    }

    public boolean existsTripInBrigade(TripEntity trip) {
        return tripQueryRepository.existsTripInBrigade(trip);
    }

    public Map<Long, TripEntity> mapByExistsBrigade(String agency) {
        return findByExistsBrigade(agency).stream()
                .collect(Collectors.toMap(TripEntity::getTripId, Function.identity()));
    }
}
