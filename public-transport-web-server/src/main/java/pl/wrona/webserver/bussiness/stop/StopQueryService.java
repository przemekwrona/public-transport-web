package pl.wrona.webserver.bussiness.stop;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.entity.StopEntity;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StopQueryService {

    private final StopTimeRepository stopTimeRepository;

    public Map<TripProfileEntity, FirstAndLastStop> findFirstAndLastStops(Collection<TripProfileEntity> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            return Map.of();
        }

        Map<Long, TripProfileEntity> profilesById = profiles.stream()
                .filter(profile -> profile != null && profile.getTripProfileId() != null)
                .collect(Collectors.toMap(
                        TripProfileEntity::getTripProfileId,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new));
        if (profilesById.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<StopTimeEntity>> stopTimesByProfileId = stopTimeRepository
                .findFirstAndLastByTripProfileIds(profilesById.keySet())
                .stream()
                .collect(Collectors.groupingBy(
                        stopTime -> stopTime.getTripProfile().getTripProfileId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                stopTimes -> stopTimes.stream()
                                        .sorted(Comparator.comparingInt(stopTime -> stopTime.getStopTimeId().getStopSequence()))
                                        .toList())));

        Map<TripProfileEntity, FirstAndLastStop> firstAndLastStops = new LinkedHashMap<>();
        profilesById.forEach((profileId, profile) -> {
            List<StopTimeEntity> endpoints = stopTimesByProfileId.getOrDefault(profileId, List.of());
            if (endpoints.isEmpty()) {
                return;
            }
            StopEntity firstStop = endpoints.get(0).getStopEntity();
            StopEntity lastStop = endpoints.get(endpoints.size() - 1).getStopEntity();
            if (firstStop == null && lastStop == null) {
                return;
            }
            firstAndLastStops.put(profile, new FirstAndLastStop(firstStop, lastStop));
        });
        return firstAndLastStops;
    }
}
