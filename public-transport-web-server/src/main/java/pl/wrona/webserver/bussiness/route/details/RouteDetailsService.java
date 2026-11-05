package pl.wrona.webserver.bussiness.route.details;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Route;
import org.igeolab.iot.pt.server.api.model.RouteDetails;
import org.igeolab.iot.pt.server.api.model.TerritoryUnit;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.stop.name.AgencyStopNameService;
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
    private final AgencyStopNameService agencyStopNameService;

    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER,
            PreAgencyAuthorize.Role.BRIGADIER,
            PreAgencyAuthorize.Role.DISPATCHER,
            PreAgencyAuthorize.Role.DRIVER
    })
    public RouteDetails getRouteDetails(String instance, String routeCode) {
        var route = routeQueryService.findRouteByAgencyCodeAndRouteCode(instance, routeCode);
        var tripWithBrigades = tripQueryService.mapByExistsBrigade(instance, route);

        var tripEntities = tripQueryService.findByAgencyCodeAndLineAndName(instance, route.getLine(), route.getName());

        var stopIds = Stream.concat(
                Stream.of(route.getOriginStopId(), route.getDestinationStopId()),
                tripEntities.stream().flatMap(trip -> Stream.of(
                        AgencyStopNameService.stopId(trip.getOriginStopId()),
                        AgencyStopNameService.stopId(trip.getDestinationStopId())))
        ).toList();
        var agencyNames = agencyStopNameService.findOverrides(instance, stopIds);

        var trips = tripEntities.stream()
                .map(tripEntity -> applyTripStopNames(TripMapper.map(tripEntity, tripWithBrigades), tripEntity, agencyNames))
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

        Route mappedRoute = RouteMapper.map(route, stopDictionary, Map.of(), territoriesDictionary);
        agencyStopNameService.apply(agencyNames, mappedRoute.getOriginStop(), mappedRoute.getDestinationStop());

        return new RouteDetails()
                .route(mappedRoute)
                .trips(trips)
                .territories(territorialUnitsStops.stream()
                        .map(it -> new TerritoryUnit()
                                .name(it.getNazwa()))
                        .toList());
    }

    private static Trip applyTripStopNames(Trip trip, TripEntity tripEntity, Map<Long, String> agencyNames) {
        return trip
                .origin(AgencyStopNameService.prefer(trip.getOrigin(), agencyNames, AgencyStopNameService.stopId(tripEntity.getOriginStopId())))
                .destination(AgencyStopNameService.prefer(trip.getDestination(), agencyNames, AgencyStopNameService.stopId(tripEntity.getDestinationStopId())));
    }

}
