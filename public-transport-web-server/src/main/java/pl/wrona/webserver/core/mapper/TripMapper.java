package pl.wrona.webserver.core.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
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
        var trip = tripsDetails.getItem();
        tripEntity.setVariantName(trip.getVariant());
        tripEntity.setVariantDesignation(trip.getVariantDesignation());
        tripEntity.setVariantDescription(trip.getVariantDescription());

        tripEntity.setVariantMode(TripModeMapper.map(trip.getMode()));
        tripEntity.setTrafficMode(TripTrafficModeMapper.map(trip.getTrafficMode()));

        tripEntity.setHeadsign(trip.getHeadsign());

        tripEntity.setCustomizedCommunicationVelocity(0);
        tripEntity.setCalculatedCommunicationVelocity(trip.getCalculatedCommunicationVelocity());
        tripEntity.setOriginStopName(trip.getOrigin());
        tripEntity.setDestinationStopName(trip.getDestination());
        tripEntity.setMainVariant(trip.getIsMainVariant());
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
                .mode(TripModeMapper.map(trip.getVariantMode()))
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
