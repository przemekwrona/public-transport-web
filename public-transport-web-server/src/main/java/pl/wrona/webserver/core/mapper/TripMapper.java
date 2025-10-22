package pl.wrona.webserver.core.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.igeolab.iot.pt.server.api.model.TrafficMode;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.igeolab.iot.pt.server.api.model.TripMode;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripTrafficMode;
import pl.wrona.webserver.core.agency.TripVariantMode;

import java.util.List;
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

        if (TripMode.FRONT.equals(trip.getMode())) {
            tripEntity.setMode(TripVariantMode.FRONT);
        } else if (TripMode.BACK.equals(trip.getMode())) {
            tripEntity.setMode(TripVariantMode.BACK);
        }

        if (TrafficMode.NORMAL.equals(trip.getTrafficMode())) {
            tripEntity.setTrafficMode(TripTrafficMode.NORMAL);
        } else if (TrafficMode.TRAFFIC.equals(trip.getTrafficMode())) {
            tripEntity.setTrafficMode(TripTrafficMode.TRAFFIC);
        }

        tripEntity.setHeadsign(trip.getHeadsign());
        tripEntity.setCalculatedCommunicationVelocity(trip.getCommunicationVelocity());
        tripEntity.setOriginStopName(trip.getOrigin());
        tripEntity.setDestinationStopName(trip.getDestination());
        tripEntity.setMainVariant(trip.getIsMainVariant());


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

    public Trip map(TripEntity trip) {
        return new Trip()
                .name(trip.getRoute().getName())
                .line(trip.getRoute().getLine())
                .variant(trip.getVariantName())
                .communicationVelocity(trip.getCalculatedCommunicationVelocity())
                .variantDesignation(trip.getVariantDesignation())
                .variantDescription(trip.getVariantDescription())
                .travelTimeInSeconds(trip.getTravelTimeInSeconds())
                .distanceInMeters(trip.getDistanceInMeters())
                .mode(TripModeMapper.map(trip.getMode()))
                .trafficMode(Optional.of(trip.getTrafficMode())
                        .map(mode -> switch (mode) {
                            case NORMAL -> TrafficMode.NORMAL;
                            case TRAFFIC -> TrafficMode.TRAFFIC;
                            default -> null;
                        })
                        .orElse(null))
                .origin(trip.getOriginStopName())
                .destination(trip.getDestinationStopName())
                .isMainVariant(trip.isMainVariant())
                .headsign(trip.getHeadsign());
    }
}
