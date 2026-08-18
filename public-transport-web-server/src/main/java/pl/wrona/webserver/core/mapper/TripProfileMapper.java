package pl.wrona.webserver.core.mapper;

import lombok.experimental.UtilityClass;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.TripProfile;
import pl.wrona.webserver.core.agency.TripProfileEntity;

import java.util.Optional;

@UtilityClass
public class TripProfileMapper {

    public TripProfileEntity map(TripProfile tripProfile) {
        return update(new TripProfileEntity(), tripProfile);
    }

    public TripProfileEntity update(TripProfileEntity tripProfileEntity, TripProfile tripProfile) {
        StopTime lastStop = tripProfile.getStops().get(tripProfile.getStops().size() - 1);
        var customizedSeconds = Optional.ofNullable(lastStop.getCustomizedSeconds()).map(Integer::doubleValue).orElse(lastStop.getCalculatedSeconds().doubleValue());
        var velocityMetersPerSeconds = lastStop.getMeters().doubleValue() / customizedSeconds;
        var velocityKmPerH = Math.round(velocityMetersPerSeconds * 3600.0d / 1000.0d);

        tripProfileEntity.setCustomizedCommunicationVelocity((int) velocityKmPerH);
//        tripProfileEntity.setCalculatedCommunicationVelocity(tripProfile.getCalculatedCommunicationVelocity());
//        tripProfileEntity.setOriginStopName(tripsDetails.getOriginStopName());
//        tripProfileEntity.setDestinationStopName(tripsDetails.getDestinationStopName());
//        tripProfileEntity.setMainVariant(tripsDetails.getIsMainVariant());
//        tripProfileEntity.setCustomized(tripProfile.getIsCustomized());
//        tripProfileEntity.setUpdatedAt(LocalDateTime.now());

//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            List<List<Float>> geometry = tripProfile.getGeometry().stream()
//                    .map(point -> List.of(point.getLat(), point.getLon()))
//                    .toList();
//            tripEntity.setGeometry(objectMapper.writeValueAsString(geometry));
//        } catch (Exception e) {
//        }

        return tripProfileEntity;
    }
}
