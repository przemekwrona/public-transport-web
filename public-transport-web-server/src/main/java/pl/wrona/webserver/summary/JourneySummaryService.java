package pl.wrona.webserver.summary;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.igeolab.iot.otp.api.model.RoutesResponse;
import org.igeolab.iot.pt.server.api.model.CityBike;
import org.igeolab.iot.pt.server.api.model.JourneySummaryResponse;
import org.igeolab.iot.pt.server.api.model.JourneySummaryResponseDifferences;
import org.igeolab.iot.pt.server.api.model.ModeSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.gbfs.GbfsService;
import pl.wrona.webserver.otp.OtpClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class JourneySummaryService {

    private final OtpClient otpClient;
    private final JourneyBikeSummaryService journeyBikeSummaryService;

    public JourneySummaryResponse summaryTrip(String instanceId, String fromPlace, String toPlace, String date, String time, String mode, String local, Boolean showIntermediateStops, Double maxWalkDistance, Boolean arriveBy, Boolean wheelchair, Integer numItineraries, Boolean realtime, String optimize) {
        ResponseEntity<RoutesResponse> transitResponse = this.otpClient.planTrip(instanceId, fromPlace, toPlace, date, time, "TRANSIT", "en", false, 1_000D, arriveBy, true, 20, realtime, optimize);
        ResponseEntity<RoutesResponse> bikeResponse = this.otpClient.planTrip(instanceId, fromPlace, toPlace, date, time, "BICYCLE_RENT", "en", false, 1_000D, arriveBy, true, 1, realtime, optimize);
        ResponseEntity<RoutesResponse> carResponse = this.otpClient.planTrip(instanceId, fromPlace, toPlace, date, time, "CAR", "en", false, 1_000D, arriveBy, true, 1, realtime, optimize);
//        ResponseEntity<RoutesResponse> walkResponse = this.otpClient.planTrip(instanceId, fromPlace, toPlace, date, time, mode, "en", showIntermediateStops, maxWalkDistance, arriveBy, wheelchair, 1, realtime, optimize);

        ModeSummary walkSummary = TripPlanStatisticUtils.statisticForFirstByWalk(transitResponse.getBody().getPlan());
        ModeSummary transitSummary = TripPlanStatisticUtils.statisticForFirstByNotWalk(transitResponse.getBody().getPlan());
        ModeSummary bikeSummary = TripPlanStatisticUtils.statisticForFirstByNotWalk(bikeResponse.getBody().getPlan());
        ModeSummary carSummary = TripPlanStatisticUtils.statisticForFirstByNotWalk(carResponse.getBody().getPlan());

        Double lon = Double.parseDouble(fromPlace.split(",")[1]);
        Double lat = Double.parseDouble(fromPlace.split(",")[0]);

        CityBike cityBike = journeyBikeSummaryService.getWarsawVeturiloStops("nextbike_vw", lon, lat);

        return new JourneySummaryResponse()
                .walk(walkSummary)
                .transit(transitSummary)
                .bike(bikeSummary)
                .car(carSummary)
                .differences(new JourneySummaryResponseDifferences()
                        .walk(TripDifferenceStatisticUtils.modeDifference(walkSummary, transitSummary, walkSummary, bikeSummary, carSummary))
                        .transit(TripDifferenceStatisticUtils.modeDifference(transitSummary, transitSummary, walkSummary, bikeSummary, carSummary))
                        .bike(TripDifferenceStatisticUtils.modeDifference(bikeSummary, transitSummary, walkSummary, bikeSummary, carSummary))
                        .car(TripDifferenceStatisticUtils.modeDifference(carSummary, transitSummary, walkSummary, bikeSummary, carSummary)))
                .cityBike(cityBike);
    }
}
