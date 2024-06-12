package pl.wrona.webserver.otp;

import lombok.AllArgsConstructor;
import org.igeolab.iot.otp.api.model.RoutesResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OtpService {

    private final OtpClient otpClient;

    public ResponseEntity<RoutesResponse> planTrip(String instanceId, String fromPlace, String toPlace, String date, String time, String mode, String local, Boolean showIntermediateStops, Double maxWalkDistance, Boolean arriveBy, Boolean wheelchair, Integer numItineraries, Boolean realtime, String optimize) {
        return this.otpClient.planTrip(instanceId, fromPlace, toPlace, date, time, mode, local, showIntermediateStops, maxWalkDistance, arriveBy, wheelchair, numItineraries, realtime, optimize);
    }

}
