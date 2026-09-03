package pl.wrona.webserver.bussiness.trip;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.agency.TripTrafficMode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TripProfileQueryService {

    private final TripProfileQueryRepository tripProfileQueryRepository;

    public List<TripProfileEntity> findAllByTrip(TripEntity trip) {
        return tripProfileQueryRepository.findAllByTrip(trip);
    }

    public Map<TripEntity, Set<TripProfileEntity>> dictAllByTrip(RouteEntity routeEntity) {
        return tripProfileQueryRepository.findAllByRoute(routeEntity).stream()
                .collect(Collectors.groupingBy(TripProfileEntity::getTrip, Collectors.toSet()));
    }

    public TripProfileEntity findAllByTripAndTrafficMode(TripEntity trip, TripTrafficMode trafficMode) {
        return tripProfileQueryRepository.findAllByTripAndTrafficMode(trip, trafficMode);
    }

    public TripProfileEntity findByAgencyAndRouteCodeAndTripCodeAndTrafficMode(String agency, String routeCode, String tripCode, TripTrafficMode trafficMode) {
        return tripProfileQueryRepository.findByAgencyAndRouteCodeAndTripCodeAndTrafficMode(agency, Hex.fromHex(routeCode), Hex.fromHex(tripCode), trafficMode);
    }

}
