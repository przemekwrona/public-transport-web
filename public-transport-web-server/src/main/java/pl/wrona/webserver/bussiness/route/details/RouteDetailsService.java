package pl.wrona.webserver.bussiness.route.details;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteDetails;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.TerritoryUnit;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.core.StopTimeService;
import pl.wrona.webserver.core.TerritorialUnitQueryService;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.core.entity.TerritorialUnitEntity;
import pl.wrona.webserver.core.mapper.RouteMapper;
import pl.wrona.webserver.core.mapper.TripMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class RouteDetailsService {

    private final RouteQueryService routeQueryService;
    private final TripQueryService tripQueryService;
    private final StopTimeService stopTimeService;
    private final StopService stopService;
    private final TerritorialUnitQueryService territorialUnitQueryService;

    @PreAgencyAuthorize
    public RouteDetails getRouteDetails(String instance, RouteId routeId) {
        var route = routeQueryService.findRouteByAgencyCodeAndRouteId(instance, routeId);
        var tripWithBrigades = tripQueryService.mapByExistsBrigade(instance);

        var tripEntities = tripQueryService.findByAgencyCodeAndLineAndName(instance, routeId.getLine(), routeId.getName());

        var trips = tripEntities.stream()
                .map(tripEntity -> TripMapper.map(tripEntity, tripWithBrigades))
                .toList();

        var mainTripStops = tripEntities.stream()
                .filter(TripEntity::isMainVariant)
                .filter(TripEntity::isFrontVariantMode)
                .min(TripComparatorUtils.tripEntityComparator())
                .map(stopTimeService::getAllStopTimesByTrip)
                .orElse(List.of()).stream()
                .map(StopTimeEntity::getStopEntity)
                .map(StopEntity::getStopId)
                .toList();

        var originAndDestinationStopsIds = List.of(route.getOriginStopId(), route.getDestinationStopId());
        var stopsIds = Stream.concat(originAndDestinationStopsIds.stream(), mainTripStops.stream()).toList();
        var stopDictionary = stopService.mapStopByIdsIn(stopsIds);

        var territoriesUnits = territorialUnitQueryService.findAllByStopIdIn(stopsIds);
        var territoriesDictionary = territoriesUnits.stream().collect(Collectors.toMap(TerritorialUnitEntity::getTerritorialUnitId, Function.identity()));

        var territorialUnitsStops = mainTripStops.stream()
                .map(stopDictionary::get)
                .map(StopEntity::getTerritorialUnitId)
                .map(territoriesDictionary::get)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new RouteDetails()
                .route(RouteMapper.map(route, stopDictionary, Map.of(), territoriesDictionary))
                .trips(trips)
                .territories(territorialUnitsStops.stream()
                        .map(it -> new TerritoryUnit()
                                .name(it.getNazwa()))
                        .toList());
    }

}
