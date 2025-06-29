package pl.wrona.webserver.agency.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.igeolab.iot.pt.server.api.model.TripMode;
import pl.wrona.webserver.agency.entity.RouteEntity;
import pl.wrona.webserver.agency.entity.TripEntity;
import pl.wrona.webserver.agency.entity.TripVariantMode;

import java.util.List;

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
        tripEntity.setHeadsign(trip.getHeadsign());
        tripEntity.setCommunicationVelocity(trip.getCommunicationVelocity());
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

    public Trip map(RouteEntity routeEntity, TripEntity trip) {
        return new Trip()
                .name(routeEntity.getName())
                .line(routeEntity.getLine())
                .variant(trip.getVariantName())
                .communicationVelocity(trip.getCommunicationVelocity())
                .variantDesignation(trip.getVariantDesignation())
                .variantDescription(trip.getVariantDescription())
                .travelTimeInSeconds(trip.getTravelTimeInSeconds())
                .distanceInMeters(trip.getDistanceInMeters())
                .mode(TripModeMapper.map(trip.getMode()))
                .origin(trip.getOriginStopName())
                .destination(trip.getDestinationStopName())
                .isMainVariant(trip.isMainVariant())
                .headsign(trip.getHeadsign());
    }
}
