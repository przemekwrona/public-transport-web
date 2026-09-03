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

        var distanceInMeters = lastStop.getMeters().doubleValue();
        var timeCalculatedInSeconds = lastStop.getCalculatedSeconds();
        var timeCustomizedInSeconds = lastStop.getCustomizedSeconds();

        tripProfileEntity.setCalculatedCommunicationVelocity(tripProfile.getCalculatedCommunicationVelocity());
        tripProfileEntity.setCustomizedCommunicationVelocity((int) convertToKmH(distanceInMeters, timeCustomizedInSeconds));
        tripProfileEntity.setTravelTimeInSeconds(timeCalculatedInSeconds);

        tripProfileEntity.setCustomized(tripProfile.getIsCustomized());
        tripProfileEntity.setDefaultProfile(tripProfile.getIsDefault());
        tripProfileEntity.setTrafficMode(TripTrafficModeMapper.map(tripProfile.getTrafficMode()));

//        tripProfileEntity.setMainVariant(tripsDetails.getIsMainVariant());
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

    public static double convertToKmH(double meters, double seconds) {
        if (seconds <= 0) {
            throw new IllegalArgumentException("Time in seconds must be greater than zero.");
        }

        // Speed in meters per second (m/s) multiplied by 3.6 to get km/h
        return (meters / seconds) * 3.6;
    }
}
