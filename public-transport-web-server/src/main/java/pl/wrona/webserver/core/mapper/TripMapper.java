package pl.wrona.webserver.core.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.igeolab.iot.pt.server.api.model.TripsDetails;
import pl.wrona.webserver.core.agency.TripEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@UtilityClass
public class TripMapper {

    public TripEntity map(TripsDetails tripsDetails) {
        return update(new TripEntity(), tripsDetails);
    }

    public TripEntity update(TripEntity tripEntity, TripsDetails tripsDetails) {
        tripEntity.setVariantName(tripsDetails.getTripId().getVariantName());
        tripEntity.setVariantMode(TripVariantModeMapper.map(tripsDetails.getTripId().getVariantMode()));
        tripEntity.setTrafficMode(TripTrafficModeMapper.map(tripsDetails.getTripId().getTrafficMode()));

        if (tripsDetails.getIsMainVariant()) {
            tripEntity.setVariantName("MAIN");
            tripEntity.setVariantDesignation(StringUtils.EMPTY);
            tripEntity.setVariantDescription(StringUtils.EMPTY);

        } else {
            tripEntity.setVariantDesignation(tripsDetails.getVariantDesignation());
            tripEntity.setVariantDescription(tripsDetails.getVariantDescription());
        }

        tripEntity.setHeadsign(tripsDetails.getHeadsign());

        StopTime lastStop = tripsDetails.getStops().get(tripsDetails.getStops().size() - 1);
        var velocityMetersPerSeconds = lastStop.getMeters().doubleValue() / lastStop.getCustomizedSeconds().doubleValue();
        var velocityKmPerH = Math.round(velocityMetersPerSeconds * 3600.0d / 1000.0d);
        tripEntity.setCustomizedCommunicationVelocity((int) velocityKmPerH);
        tripEntity.setCalculatedCommunicationVelocity(tripsDetails.getCalculatedCommunicationVelocity());
        tripEntity.setOriginStopName(tripsDetails.getOriginStopName());
        tripEntity.setDestinationStopName(tripsDetails.getDestinationStopName());
        tripEntity.setMainVariant(tripsDetails.getIsMainVariant());
        tripEntity.setCustomized(tripsDetails.getIsCustomized());
        tripEntity.setUpdatedAt(LocalDateTime.now());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<List<Float>> geometry = tripsDetails.getGeometry().stream()
                    .map(point -> List.of(point.getLat(), point.getLon()))
                    .toList();
            tripEntity.setGeometry(objectMapper.writeValueAsString(geometry));
        } catch (Exception e) {
        }
        return tripEntity;
    }

    public Trip map(TripEntity trip, Map<Long, TripEntity> tripWithBrigades) {
        return new Trip()
                .name(trip.getRoute().getName())
                .line(trip.getRoute().getLine())
                .variant(trip.getVariantName())
                .calculatedCommunicationVelocity(trip.getCalculatedCommunicationVelocity())
                .variantDesignation(trip.getVariantDesignation())
                .variantDescription(trip.getVariantDescription())
                .travelTimeInSeconds(trip.getTravelTimeInSeconds())
                .distanceInMeters(trip.getDistanceInMeters())
                .mode(TripVariantModeMapper.map(trip.getVariantMode()))
                .trafficMode(TripTrafficModeMapper.map(trip.getTrafficMode()))
                .origin(trip.getOriginStopName())
                .destination(trip.getDestinationStopName())
                .isMainVariant(trip.isMainVariant())
                .headsign(trip.getHeadsign())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .matchAnyBrigade(tripWithBrigades.containsKey(trip.getTripId()));
    }
}
