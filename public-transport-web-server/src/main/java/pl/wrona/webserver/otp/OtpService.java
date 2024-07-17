package pl.wrona.webserver.otp;

import lombok.AllArgsConstructor;
import org.igeolab.iot.otp.api.model.Route;
import org.igeolab.iot.otp.api.model.RoutesResponse;
import org.igeolab.iot.otp.api.model.Stop;
import org.igeolab.iot.otp.api.model.StopDetails;
import org.igeolab.iot.otp.api.model.StopTime;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class OtpService {

    private final OtpClient otpClient;

    public ResponseEntity<RoutesResponse> planTrip(String instanceId, String fromPlace, String toPlace, String date, String time, String mode, String local, Boolean showIntermediateStops, Double maxWalkDistance, Boolean arriveBy, Boolean wheelchair, Integer numItineraries, Boolean realtime, String optimize) {
        return this.otpClient.planTrip(instanceId, fromPlace, toPlace, date, time, mode, local, showIntermediateStops, maxWalkDistance, arriveBy, wheelchair, numItineraries, realtime, optimize);
    }

    public ResponseEntity<List<Stop>> getStops(String instanceId, BigDecimal maxLat, BigDecimal minLon, BigDecimal minLat, BigDecimal maxLon) {
        return this.otpClient.getStops(instanceId, maxLat, minLon, minLat, maxLon);
    }

    public ResponseEntity<List<StopDetails>> getStopDetails(String instanceId, String stopId) {
        return this.otpClient.getStopDetails(instanceId, stopId);
    }

    public ResponseEntity<Route> getRoutes(String instanceId, String stopId) {
        return this.otpClient.getRoutes(instanceId, stopId);
    }

    public ResponseEntity<List<StopTime>> getStoptimes(String instanceId, String stopId, String date) {
        return this.otpClient.getStoptimes(instanceId, stopId, date);
    }

}
