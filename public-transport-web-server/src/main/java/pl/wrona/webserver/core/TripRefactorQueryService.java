package pl.wrona.webserver.core;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.GetAllTripsResponse;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.igeolab.iot.pt.server.api.model.Trips;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.mapper.RouteMapper;
import pl.wrona.webserver.core.mapper.TripMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TripRefactorQueryService {

    private TripRepository tripRepository;
    private StopService stopService;
    private AgencyService agencyService;

    public GetAllTripsResponse getTripsByLineOrName(String lineOrName) {
        Map<RouteEntity, Set<TripEntity>> tripSet = tripRepository.findByLineOrNameContainingIgnoreCase(lineOrName, agencyService.getLoggedAgency()).stream()
                .collect(Collectors.groupingBy(TripEntity::getRoute, Collectors.toSet()));

        List<Long> stopsIds = tripSet.keySet().stream()
                .map(routeEntity -> List.of(routeEntity.getOriginStopId(), routeEntity.getDestinationStopId()))
                .flatMap(List::stream)
                .toList();

        Map<Long, StopEntity> stopDictionary = this.stopService.mapStopByIdsIn(stopsIds);

        List<Trips> tripsResponse = tripSet.keySet().stream()
                .map(route -> new Trips()
                        .route(RouteMapper.map(route, stopDictionary))
                        .trips(tripSet.get(route).stream()
                                .map(trip -> TripMapper.map(trip))
                                .sorted(Comparator
                                        .comparing(Trip::getIsMainVariant).reversed()
                                        .thenComparing(Trip::getName))
                                .toList()))
                .sorted(Comparator
                        .comparing((Trips trip) -> trip.getRoute().getLine())
                        .thenComparing((Trips trip) -> trip.getRoute().getName()))
                .toList();

        return new GetAllTripsResponse()
                .filter(lineOrName)
                .lines(tripsResponse);
    }
}
