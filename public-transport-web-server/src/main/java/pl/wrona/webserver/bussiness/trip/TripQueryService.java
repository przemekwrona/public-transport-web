package pl.wrona.webserver.bussiness.trip;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripTrafficMode;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.core.mapper.TripModeMapper;
import pl.wrona.webserver.core.mapper.TripTrafficModeMapper;

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

    public TripEntity findByAgencyCodeAndTripId(String instance, TripId tripId) {
        return tripQueryRepository.findTripByUniqueIndex(instance, tripId.getRouteId().getLine(), tripId.getRouteId().getName(), TripModeMapper.map(tripId.getTripMode()), TripTrafficModeMapper.map(tripId.getTrafficMode()));
    }

    public boolean existsUniqueTripIndex(String agencyCode, String line, String name, String variantName, TripVariantMode tripMode, TripTrafficMode trafficMode) {
        return tripQueryRepository.existsTripUniqueIndex(agencyCode, line, name, variantName, tripMode, trafficMode);
    }

    public List<TripEntity> findByExistsBrigade(String agency) {
        return tripQueryRepository.findByExistsBrigade(agency);
    }

    public Map<Long, TripEntity> mapByExistsBrigade(String agency) {
        return findByExistsBrigade(agency).stream()
                .collect(Collectors.toMap(TripEntity::getTripId, Function.identity()));
    }

    public boolean existsTripInBrigade(TripEntity trip) {
        return tripQueryRepository.existsTripInBrigade(trip);
    }
}
