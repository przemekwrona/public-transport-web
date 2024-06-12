package pl.wrona.webserver.otp;

import lombok.AllArgsConstructor;
import org.igeolab.iot.otp.api.OtpApi;
import org.igeolab.iot.otp.api.model.RoutesResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class OtpController implements OtpApi {

    private final OtpService otpService;

    @Override
    public ResponseEntity<RoutesResponse> planTrip(String instanceId, String fromPlace, String toPlace, String date, String time, String mode, String local, Boolean showIntermediateStops, Double maxWalkDistance, Boolean arriveBy, Boolean wheelchair, Integer numItineraries, Boolean realtime, String optimize) {
        return this.otpService.planTrip(instanceId, fromPlace, toPlace, date, time, mode, local, showIntermediateStops, maxWalkDistance, arriveBy, wheelchair, numItineraries, realtime, optimize);
    }
}
