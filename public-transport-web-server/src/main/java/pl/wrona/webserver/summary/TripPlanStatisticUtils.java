package pl.wrona.webserver.summary;

import lombok.experimental.UtilityClass;
import org.igeolab.iot.otp.api.model.Itinerary;
import org.igeolab.iot.otp.api.model.Leg;
import org.igeolab.iot.otp.api.model.TripPlan;
import org.igeolab.iot.pt.server.api.model.ModeSummary;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@UtilityClass
public class TripPlanUtils {

    ModeSummary statisticForFirstByWalk(TripPlan tripPlan) {
        return tripPlan.getItineraries().stream()
                .filter(itinerary -> itinerary.getLegs().size() == 1 && "WALK".equals(itinerary.getLegs().get(0).getMode()))
                .findFirst()
                .map(TripPlanUtils::getStatistic)
                .orElse(null);
    }
    ModeSummary statisticForFirstByNotWalk(TripPlan tripPlan) {
        return tripPlan.getItineraries().stream()
                .filter(itinerary -> !(itinerary.getLegs().size() == 1 && "WALK".equals(itinerary.getLegs().get(0).getMode())))
                .findFirst()
                .map(TripPlanUtils::getStatistic)
                .orElse(null);
    }

    List<ModeSummary> statistic(TripPlan tripPlan) {
        return tripPlan.getItineraries().stream()
                .map(TripPlanUtils::getStatistic)
                .toList();
    }

    private ModeSummary getStatistic(Itinerary itinerary) {
        return new ModeSummary()
                .totalDistance(getTotalDistance(itinerary))
                .totalDuration(getTotalDuration(itinerary))

                .walkDistanceToStation(getWalkDistanceToStation(itinerary))
                .walkDurationToStation(getWalkDurationToStation(itinerary))

                .walkDistanceFromStation(getWalkDistanceFromStation(itinerary))
                .walkDurationFromStation(getWalkDurationFromStation(itinerary))

                .totalWalkDistance(getTotalWalkDistance(itinerary))
                .totalWalkDuration(getTotalWalkDuration(itinerary))

                .travelModeDistance(getTotalModeDistance(itinerary))
                .travelModeDuration(getTotalModeDuration(itinerary))

                .transfers(itinerary.getTransfers())
                .transferWalkDistance(getTransfersWalkDistance(itinerary))
                .transferWalkDuration(getTransfersWalkDuration(itinerary))
                .transfersWaitingAtStop(getTransfersWaitingAtStop(itinerary));
    }

    private List<Leg> getAllLegs(Itinerary itineraries) {
        return Optional.ofNullable(itineraries.getLegs())
                .orElse(List.of());
    }

    public Integer getTotalDistance(Itinerary itineraries) {
        return getAllLegs(itineraries).stream()
                .map(Leg::getDistance)
                .map(Double::intValue)
                .reduce(Integer::sum)
                .orElse(0);
    }

    public Integer getTotalDuration(Itinerary itineraries) {
        return getAllLegs(itineraries).stream()
                .map(Leg::getDuration)
                .reduce(Integer::sum)
                .orElse(0);
    }

    private Optional<Leg> getFirstIfWalk(List<Leg> legs) {
        return Optional.ofNullable(legs.get(0))
                .filter(leg -> "WALK".equals(leg.getMode()));
    }

    private Integer getWalkDistanceToStation(Itinerary itineraries) {
        return getFirstIfWalk(itineraries.getLegs())
                .map(Leg::getDistance)
                .map(Double::intValue)
                .orElse(0);
    }

    private Integer getWalkDurationToStation(Itinerary itineraries) {
        return getFirstIfWalk(itineraries.getLegs())
                .map(Leg::getDuration)
                .orElse(0);
    }

    private Optional<Leg> getLastIfWalk(List<Leg> legs) {
        return Optional.ofNullable(legs.get(legs.size() - 1))
                .filter(leg -> "WALK".equals(leg.getMode()));
    }

    private Integer getWalkDistanceFromStation(Itinerary itineraries) {
        return getLastIfWalk(itineraries.getLegs())
                .map(Leg::getDistance)
                .map(Double::intValue)
                .orElse(0);
    }

    private Integer getWalkDurationFromStation(Itinerary itineraries) {
        return getLastIfWalk(itineraries.getLegs())
                .map(Leg::getDuration)
                .orElse(0);
    }

    private Stream<Leg> getAllLegsByWalk(List<Leg> itineraries) {
        return itineraries.stream()
                .filter(leg -> "WALK".equals(leg.getMode()));
    }

    private Integer getTotalWalkDistance(Itinerary itineraries) {
        return getAllLegsByWalk(itineraries.getLegs())
                .map(Leg::getDistance)
                .map(Double::intValue)
                .reduce(0, Integer::sum);
    }

    private Integer getTotalWalkDuration(Itinerary itineraries) {
        return getAllLegsByWalk(itineraries.getLegs())
                .map(Leg::getDuration)
                .reduce(0, Integer::sum);
    }

    private Stream<Leg> getAllLegsByNotWalk(List<Leg> itineraries) {
        return itineraries.stream()
                .filter(leg -> !"WALK".equals(leg.getMode()));
    }

    private Integer getTotalModeDistance(Itinerary itineraries) {
        return getAllLegsByNotWalk(itineraries.getLegs())
                .map(Leg::getDistance)
                .map(Double::intValue)
                .reduce(0, Integer::sum);
    }

    private Integer getTotalModeDuration(Itinerary itineraries) {
        return getAllLegsByNotWalk(itineraries.getLegs())
                .map(Leg::getDuration)
                .reduce(0, Integer::sum);
    }

    private List<Leg> getLegsWithoutFirstAndLastIfWalk(Itinerary itineraries) {
        if (itineraries.getTransfers() < 1) {
            return List.of();
        }

        List<Leg> legs = itineraries.getLegs();

        int firstIndex = 0;
        if ("WALK".equals(legs.get(firstIndex).getMode())) {
            legs.remove(firstIndex);
        }

        int lastIndex = legs.size() - 1;
        if ("WALK".equals(legs.get(lastIndex).getMode())) {
            legs.remove(lastIndex);
        }

        return legs;
    }


    private Integer getTransfersWalkDistance(Itinerary itineraries) {
        return getLegsWithoutFirstAndLastIfWalk(itineraries).stream()
                .filter(leg -> "WALK".equals(leg.getMode()))
                .map(Leg::getDistance)
                .map(Double::intValue)
                .reduce(0, Integer::sum);
    }


    private Integer getTransfersWalkDuration(Itinerary itineraries) {
        return getLegsWithoutFirstAndLastIfWalk(itineraries).stream()
                .filter(leg -> "WALK".equals(leg.getMode()))
                .map(Leg::getDuration)
                .reduce(0, Integer::sum);
    }

    private Integer getTransfersWaitingAtStop(Itinerary itinerary) {
        return itinerary.getWaitingTime();
    }
}
