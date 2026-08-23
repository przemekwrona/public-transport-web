package pl.wrona.webserver.bussiness.trip.details;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Point2D;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.igeolab.iot.pt.server.api.model.TripProfile;
import org.igeolab.iot.pt.server.api.model.TripsDetails;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.trip.TripProfileQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.entity.StopEntity;
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

    @PreAgencyAuthorize
    public TripsDetails getTripVariantDetails(String instance, TripId tripId) {
        var tripEntity = tripQueryService.findByAgencyCodeAndTripId(instance, tripId);

        var profiles = tripProfileQueryService.findAllByTrip(tripEntity);
        var stopTimes = stopTimeRepository.findAllByTripId(tripEntity.getTripId());
        var stops = stopService.findStopByTripId(tripEntity.getTripId());
        var tripProfiles = buildTripProfiles(profiles, stopTimes, stops);

        return new TripsDetails()
                .tripId(new TripId()
                        .routeId(new RouteId()
                                .line(tripEntity.getRoute().getLine())
                                .name(tripEntity.getRoute().getName())
                                .version(tripEntity.getRoute().getVersion()))
                        .variantName(tripEntity.getVariantName())
                        .variantMode(TripVariantModeMapper.map(tripEntity.getVariantMode()))
                        .trafficMode(tripId.getTrafficMode()))
                .isMainVariant(tripEntity.isMainVariant())
                .tripProfiles(tripProfiles)
                .geometry(buildGeometry(tripEntity.getGeometry()))
                .variantDesignation(tripEntity.getVariantDesignation())
                .variantDescription(tripEntity.getVariantDescription())
                .originStopName(tripEntity.getOriginStopName())
                .destinationStopName(tripEntity.getDestinationStopName())
                .headsign(tripEntity.getHeadsign());
    }

    private List<TripProfile> buildTripProfiles(List<TripProfileEntity> profiles, List<StopTimeEntity> stopTimes, List<StopEntity> stops) {
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
                                .map(stopTime -> mapStopTime(stopTime, stopById))
                                .toList()))
                .toList();
    }

    private StopTime mapStopTime(StopTimeEntity stopTime, Map<Long, StopEntity> stopById) {
        StopEntity stop = stopById.getOrDefault(stopTime.getStopEntity().getStopId(), stopTime.getStopEntity());

        return new StopTime()
                .stopId(stop.getStopId())
                .stopName(stop.getName())
                .calculatedSeconds(stopTime.getCalculatedTimeSeconds())
                .customizedSeconds(stopTime.getCustomizedTimeSeconds())
                .meters(stopTime.getDistanceMeters())
                .bdot10k(stop.isBdot10k())
                .lon((float) stop.getLon())
                .lat((float) stop.getLat());
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
