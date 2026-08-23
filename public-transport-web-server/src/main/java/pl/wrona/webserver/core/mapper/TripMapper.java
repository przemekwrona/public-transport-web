package pl.wrona.webserver.core.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.igeolab.iot.pt.server.api.model.RouteId1;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.igeolab.iot.pt.server.api.model.TripId1;
import org.igeolab.iot.pt.server.api.model.TripsDetails;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class TripMapper {

    public TripEntity map(TripsDetails tripsDetails) {
        return update(new TripEntity(), tripsDetails);
    }

    public TripEntity update(TripEntity tripEntity, TripsDetails tripsDetails) {
        tripEntity.setVariantName(tripsDetails.getTripId().getVariantName());
        tripEntity.setVariantMode(TripVariantModeMapper.map(tripsDetails.getTripId().getVariantMode()));
        tripEntity.setMainVariant(tripsDetails.getIsMainVariant());

        if (Optional.of(tripsDetails).map(TripsDetails::getIsMainVariant).orElse(Boolean.FALSE)) {
            tripEntity.setVariantName("MAIN");
            tripEntity.setVariantDesignation(StringUtils.EMPTY);
            tripEntity.setVariantDescription(StringUtils.EMPTY);
        } else {
            tripEntity.setVariantDesignation(tripsDetails.getVariantDesignation());
            tripEntity.setVariantDescription(tripsDetails.getVariantDescription());
        }

        tripEntity.setHeadsign(tripsDetails.getHeadsign());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<List<Float>> geometry = tripsDetails.getGeometry().stream()
                    .map(point -> List.of(point.getLat(), point.getLon()))
                    .toList();
            tripEntity.setGeometry(objectMapper.writeValueAsString(geometry));
        } catch (Exception e) {google
        }
        return tripEntity;
    }

    public Trip map(TripEntity trip, Map<Long, TripEntity> tripWithBrigades) {
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
                .calculatedCommunicationVelocity(calculatedCommunicationVelocity(trip))
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
                .matchAnyBrigade(tripWithBrigades.containsKey(trip.getTripId()));
    }

    private int calculatedCommunicationVelocity(TripEntity trip) {
        if (trip.getTripProfiles() == null || trip.getTripProfiles().isEmpty()) {
            return 0;
        }
        return trip.getTripProfiles().stream()
                .filter(TripProfileEntity::isDefaultProfile)
                .findFirst()
                .or(() -> trip.getTripProfiles().stream().findFirst())
                .map(TripProfileEntity::getCalculatedCommunicationVelocity)
                .orElse(0);
    }
}
