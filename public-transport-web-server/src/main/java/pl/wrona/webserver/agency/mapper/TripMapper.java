package pl.wrona.webserver.agency.mapper;

import lombok.experimental.UtilityClass;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.igeolab.iot.pt.server.api.model.TripMode;
import pl.wrona.webserver.agency.entity.Route;
import pl.wrona.webserver.agency.entity.TripEntity;
import pl.wrona.webserver.agency.entity.TripVariantMode;

@UtilityClass
public class TripMapper {

    public TripEntity map(Trip trip) {
        return update(new TripEntity(), trip);
    }

    public TripEntity update(TripEntity tripEntity, Trip trip) {
        tripEntity.setVariantName(trip.getVariant());
        tripEntity.setVariantDesignation(trip.getVariantDesignation());
        tripEntity.setVariantDescription(trip.getVariantDescription());

        if (TripMode.MAIN.equals(trip.getMode())) {
            tripEntity.setMode(TripVariantMode.MAIN);
        } else if (TripMode.BACK.equals(trip.getMode())) {
            tripEntity.setMode(TripVariantMode.BACK);
        }
        tripEntity.setHeadsign(trip.getHeadsign());
        tripEntity.setCommunicationVelocity(trip.getCommunicationVelocity());
        tripEntity.setOrigin(trip.getOrigin());
        tripEntity.setDestination(trip.getDestination());
        tripEntity.setMainVariant(trip.getIsMainVariant());
        return tripEntity;
    }

    public Trip map(Route route, TripEntity trip) {
        return new Trip()
                .name(route.getName())
                .line(route.getLine())
                .variant(trip.getVariantName())
                .communicationVelocity(trip.getCommunicationVelocity())
                .variantDesignation(trip.getVariantDesignation())
                .variantDescription(trip.getVariantDescription())
                .travelTimeInSeconds(trip.getTravelTimeInSeconds())
                .distanceInMeters(trip.getDistanceInMeters())
                .mode(TripModeMapper.map(trip.getMode()))
                .origin(trip.getOrigin())
                .destination(trip.getDestination())
                .isMainVariant(trip.isMainVariant())
                .headsign(trip.getHeadsign());
    }
}
