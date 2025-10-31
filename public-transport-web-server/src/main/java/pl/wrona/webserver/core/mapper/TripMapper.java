package pl.wrona.webserver.core.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.igeolab.iot.pt.server.api.model.TrafficMode;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.igeolab.iot.pt.server.api.model.TripMode;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripTrafficMode;
import pl.wrona.webserver.core.agency.TripVariantMode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class TripMapper {

    public TripEntity map(Trip trip) {
        return update(new TripEntity(), trip);
    }

    public TripEntity update(TripEntity tripEntity, Trip trip) {
        tripEntity.setVariantName(trip.getVariant());
        tripEntity.setVariantDesignation(trip.getVariantDesignation());
        tripEntity.setVariantDescription(trip.getVariantDescription());

        tripEntity.setMode(TripModeMapper.map(trip.getMode()));
        tripEntity.setTrafficMode(TripTrafficModeMapper.map(trip.getTrafficMode()));

        tripEntity.setHeadsign(trip.getHeadsign());
        tripEntity.setCustomizedCommunicationVelocity(trip.getCustomizedCommunicationVelocity());
        tripEntity.setCalculatedCommunicationVelocity(trip.getCalculatedCommunicationVelocity());
        tripEntity.setOriginStopName(trip.getOrigin());
        tripEntity.setDestinationStopName(trip.getDestination());
        tripEntity.setMainVariant(trip.getIsMainVariant());
        tripEntity.setUpdatedAt(LocalDateTime.now());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<List<Float>> geometry = trip.getGeometry().stream()
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
                .mode(TripModeMapper.map(trip.getMode()))
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
