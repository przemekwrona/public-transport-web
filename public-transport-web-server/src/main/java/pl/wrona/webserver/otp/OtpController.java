package pl.wrona.webserver.otp;

import lombok.AllArgsConstructor;
import org.igeolab.iot.otp.api.OtpApi;
import org.igeolab.iot.otp.api.model.Route;
import org.igeolab.iot.otp.api.model.RoutesResponse;
import org.igeolab.iot.otp.api.model.Stop;
import org.igeolab.iot.otp.api.model.StopDetails;
import org.igeolab.iot.otp.api.model.StopTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class OtpController implements OtpApi {

    private final OtpService otpService;

    @Override
    public ResponseEntity<RoutesResponse> planTrip(String instanceId, String fromPlace, String toPlace, String date, String time, String mode, String local, Boolean showIntermediateStops, Double maxWalkDistance, Boolean arriveBy, Boolean wheelchair, Integer numItineraries, Boolean realtime, String optimize) {
        return this.otpService.planTrip(instanceId, fromPlace, toPlace, date, time, mode, local, showIntermediateStops, maxWalkDistance, arriveBy, wheelchair, numItineraries, realtime, optimize);
    }

    @Override
    public ResponseEntity<Route> getRoutes(String instanceId, String stopId) {
        return this.otpService.getRoutes(instanceId, stopId);
    }

    @Override
    public ResponseEntity<List<StopDetails>> getStopDetails(String instanceId, String stopId) {
        return this.otpService.getStopDetails(instanceId, stopId);
    }

    @Override
    public ResponseEntity<List<Stop>> getStops(String instanceId, BigDecimal maxLat, BigDecimal minLon, BigDecimal minLat, BigDecimal maxLon) {
        return this.otpService.getStops(instanceId, maxLat, minLon, minLat, maxLon);
    }

    @Override
    public ResponseEntity<List<StopTime>> getStoptimes(String instanceId, String stopId, String date) {
        return this.otpService.getStoptimes(instanceId, stopId, date);
    }
}
