package pl.wrona.webserver.bussiness.trip.pagination;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.GetAllTripsResponse;
import org.igeolab.iot.pt.server.api.model.ProfileShortcut;
import org.igeolab.iot.pt.server.api.model.RouteDetails;
import org.igeolab.iot.pt.server.api.model.RouteId1;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.igeolab.iot.pt.server.api.model.TripId1;
import org.igeolab.iot.pt.server.api.model.TripProfile;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.bussiness.trip.TripProfileQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.bussiness.trip.TripRepository;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.mapper.RouteMapper;
import pl.wrona.webserver.core.mapper.TripMapper;
import pl.wrona.webserver.core.mapper.TripTrafficModeMapper;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TripPaginationService {

    private TripRepository tripRepository;
    private StopService stopService;
    private AgencyService agencyService;

    private TripQueryService tripQueryService;
    private TripProfileQueryService tripProfileQueryService;
    private RouteQueryService routeQueryService;

    @PreAgencyAuthorize
    public GetAllTripsResponse getTripsByRouteAndFilterLineOrName(String instance, String routeCode) {
        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);
        var routeEntity = routeQueryService.findRouteByAgencyCodeAndRouteCode(instance, routeCode);

        Map<RouteEntity, Set<TripEntity>> dictTrips = tripRepository.findByAgencyAndRouteCode(agencyEntity, routeCode).stream()
                .collect(Collectors.groupingBy(TripEntity::getRoute, Collectors.toSet()));

        Map<TripEntity, Set<TripProfileEntity>> dictProfiles = tripProfileQueryService.dictAllByTrip(routeEntity);

        Map<Long, TripEntity> tripInBrigadeDictionary = tripQueryService.mapByExistsBrigade(instance);

        List<Long> stopsIds = dictTrips.keySet().stream()
                .map(route -> List.of(route.getOriginStopId(), route.getDestinationStopId()))
                .flatMap(List::stream)
                .toList();

        Map<Long, StopEntity> stopDictionary = this.stopService.mapStopByIdsIn(stopsIds);

        List<RouteDetails> tripsResponse = dictTrips.keySet().stream()
                .map(route -> new RouteDetails()
                        .route(RouteMapper.map(route, stopDictionary, Map.of(), Map.of()))
                        .trips(dictTrips.get(route).stream()
                                .map(trip -> TripPaginationService.map(trip, dictProfiles, tripInBrigadeDictionary))
                                .sorted(Comparator
                                        .comparing(Trip::getIsMainVariant).reversed()
                                        .thenComparing(Trip::getName))
                                .toList()))
                .sorted(Comparator
                        .comparing((RouteDetails trip) -> trip.getRoute().getRouteId().getLine())
                        .thenComparing((RouteDetails trip) -> trip.getRoute().getRouteId().getName()))
                .toList();

        return new GetAllTripsResponse()
                .lines(tripsResponse);
    }

    public static Trip map(TripEntity trip, Map<TripEntity, Set<TripProfileEntity>> dictProfiles, Map<Long, TripEntity> tripWithBrigades) {
        return new Trip()
                .tripId(new TripId1()
                        .routeId(new RouteId1()
                                .line(trip.getRoute().getLine())
                                .name(trip.getRoute().getName())
                                .version(trip.getRoute().getVersion())
                                .routeCode(trip.getRoute().getRouteCode()))
                        .variantName(trip.getVariantName())
                        .variantMode(TripVariantModeMapper.map(trip.getVariantMode()))
                        .tripCode(trip.getTripCode()))
                .name(trip.getRoute().getName())
                .line(trip.getRoute().getLine())
                .variant(trip.getVariantName())
                .calculatedCommunicationVelocity(TripMapper.calculatedCommunicationVelocity(trip))
                .variantDesignation(trip.getVariantDesignation())
                .variantDescription(trip.getVariantDescription())
                .travelTimeInSeconds(trip.getTravelTimeInSeconds())
                .distanceInMeters(trip.getDistanceInMeters())
                .mode(TripVariantModeMapper.map(trip.getVariantMode()))
                .origin(trip.getOriginStopName())
                .destination(trip.getDestinationStopName())
                .isMainVariant(trip.isMainVariant())
                .headsign(trip.getHeadsign())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .matchAnyBrigade(tripWithBrigades.containsKey(trip.getTripId()))
                .profile(dictProfiles.get(trip).stream()
                        .map(profile -> new ProfileShortcut()
                                .trafficMode(TripTrafficModeMapper.map(profile.getTrafficMode()))
                                .travelTime(profile.getTravelTimeInSeconds()))
                        .toList());
    }

}
