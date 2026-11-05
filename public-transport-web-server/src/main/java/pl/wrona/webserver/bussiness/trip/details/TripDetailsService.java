package pl.wrona.webserver.bussiness.trip.details;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Point2D;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.TerritoryUnit;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.igeolab.iot.pt.server.api.model.TripId2;
import org.igeolab.iot.pt.server.api.model.TripProfile;
import org.igeolab.iot.pt.server.api.model.TripsDetails;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.stop.name.AgencyStopNameService;
import pl.wrona.webserver.bussiness.trip.TripProfileQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.TerritorialUnitQueryService;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripGeometryEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.core.entity.TerritorialUnitEntity;
import pl.wrona.webserver.core.mapper.TripTrafficModeMapper;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TripDetailsService {

    private final TripQueryService tripQueryService;
    private final TripProfileQueryService tripProfileQueryService;
    private final StopTimeRepository stopTimeRepository;
    private final StopService stopService;
    private final ObjectMapper objectMapper;
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
    public TripsDetails getTripVariantDetails(String instance, String routeCode, String tripCode) {
        var tripEntity = tripQueryService.findTripByAgencyAndRouteCodeAndTripCode(instance, routeCode, tripCode);

        var profiles = tripProfileQueryService.findAllByTrip(tripEntity);
        var stopTimes = stopTimeRepository.findAllByTripId(tripEntity.getTripId());
        var stops = stopService.findStopByTripId(tripEntity.getTripId());
        var tripProfiles = buildTripProfiles(instance, profiles, stopTimes, stops);

        var originTerritory = territorialUnitQueryService.findAllByStopIdIn(List.of(tripEntity.getRoute().getOriginStopId())).get(0);
        var targetTerritory = territorialUnitQueryService.findAllByStopIdIn(List.of(tripEntity.getRoute().getDestinationStopId())).get(0);

        return new TripsDetails()
                .tripId(new TripId2()
                        .routeId(new RouteId()
                                .line(tripEntity.getRoute().getLine())
                                .name(tripEntity.getRoute().getName())
                                .version(tripEntity.getRoute().getVersion())
                                .routeCode(tripEntity.getRoute().getRouteCode()))
                        .variantMode(TripVariantModeMapper.map(tripEntity.getVariantMode()))
                        .tripCode(tripEntity.getTripCode()))
                .isMainVariant(tripEntity.isMainVariant())
                .tripProfiles(tripProfiles)
                .geometry(buildGeometry(geometryOf(tripEntity)))
                .variantDesignation(tripEntity.getVariantDesignation())
                .variantDescription(tripEntity.getVariantDescription())
                .originTerritory(new TerritoryUnit()
                        .name(originTerritory.getNazwa()))
                .targetTerritory(new TerritoryUnit()
                        .name(targetTerritory.getNazwa()))
                .headsign(tripEntity.getHeadsign());
    }

    private List<TripProfile> buildTripProfiles(String instance, List<TripProfileEntity> profiles, List<StopTimeEntity> stopTimes, List<StopEntity> stops) {
        Map<Long, String> agencyNames = agencyStopNameService.findOverrides(instance, stops.stream().map(StopEntity::getStopId).toList());
        Map<Long, StopEntity> stopById = stops.stream()
                .collect(Collectors.toMap(StopEntity::getStopId, Function.identity(), (left, right) -> left));
        Map<Long, List<StopTimeEntity>> stopTimesByProfileId = stopTimes.stream()
                .collect(Collectors.groupingBy(stopTime -> stopTime.getStopTimeId().getTripProfileId()));

        return profiles.stream()
                .map(profile -> new TripProfile()
                        .trafficMode(TripTrafficModeMapper.map(profile.getTrafficMode()))
                        .travelTimeInSeconds(profile.getTravelTimeInSeconds())
                        .calculatedCommunicationVelocity(profile.getCalculatedCommunicationVelocity())
                        .customizedCommunicationVelocity(profile.getCustomizedCommunicationVelocity())
                        .isDefault(profile.isDefaultProfile())
                        .isCustomized(profile.isCustomized())
                        .stops(stopTimesByProfileId.getOrDefault(profile.getTripProfileId(), List.of()).stream()
                                .map(stopTime -> mapStopTime(stopTime, stopById, agencyNames))
                                .toList()))
                .toList();
    }

    private StopTime mapStopTime(StopTimeEntity stopTime, Map<Long, StopEntity> stopById, Map<Long, String> agencyNames) {
        StopEntity stop = stopById.getOrDefault(stopTime.getStopEntity().getStopId(), stopTime.getStopEntity());

        return new StopTime()
                .stopId(stop.getStopId())
                .stopName(AgencyStopNameService.prefer(stop.getName(), agencyNames, stop.getStopId()))
                .calculatedSeconds(stopTime.getCalculatedTimeSeconds())
                .customizedSeconds(stopTime.getCustomizedTimeSeconds())
                .meters(stopTime.getDistanceMeters())
                .bdot10k(stop.isBdot10k())
                .lon((float) stop.getLon())
                .lat((float) stop.getLat());
    }

    private String geometryOf(TripEntity tripEntity) {
        TripGeometryEntity tripGeometry = tripEntity.getTripGeometry();
        if (tripGeometry == null) {
            return null;
        }
        return tripGeometry.getGeometry();
    }

    private @NonNull List<Point2D> buildGeometry(String path) {
        List<Point2D> geometry = new ArrayList<>();

        if (path != null) {
            try {
                List<List<Float>> points = objectMapper.readValue(path, new TypeReference<List<List<Float>>>() {
                });
                geometry = points.stream()
                        .map((List<Float> point) -> new Point2D()
                                .lat(point.get(0))
                                .lon(point.get(1)))
                        .toList();
            } catch (Exception exception) {
                throw new BusinessException("ERROR:202511031624", exception.getMessage());
            }
        }
        return geometry;
    }

}
