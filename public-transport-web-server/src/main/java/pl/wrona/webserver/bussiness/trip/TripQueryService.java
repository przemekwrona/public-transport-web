package pl.wrona.webserver.bussiness.trip;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TripQueryService {

    private final TripQueryRepository tripQueryRepository;

    @PreAgencyAuthorize
    public List<TripEntity> findByAgencyCodeAndLineAndName(String instance, String line, String name) {
        return tripQueryRepository.findByAgencyCodeAndLineAndName(instance, line, name);
    }

    public TripEntity findByAgencyCodeAndTripId(String instance, TripId tripId) {
        return tripQueryRepository.findTripByUniqueIndex(instance, tripId.getRouteId().getLine(), tripId.getRouteId().getName(), tripId.getVariantName(), TripVariantModeMapper.map(tripId.getVariantMode()));
    }

    public TripEntity findTripByAgencyAndRouteCodeAndTripCode(String instance, String routeCode, String tripCode) {
        return tripQueryRepository.findTripByAgencyAndRouteCodeAndTripCode(instance, routeCode, tripCode);
    }

    public boolean existsUniqueTripIndex(String agencyCode, String line, String name, String variantName, TripVariantMode tripMode) {
        return tripQueryRepository.existsTripUniqueIndex(agencyCode, line, name, variantName, tripMode);
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
