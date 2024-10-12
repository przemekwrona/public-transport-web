package pl.wrona.webserver.summary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.igeolab.iot.gbfs.server.api.model.StationInformationV23DataStationsInner;
import org.igeolab.iot.gbfs.server.api.model.StationStatusV23DataStationsInner;
import org.igeolab.iot.pt.server.api.model.CityBike;
import org.igeolab.iot.pt.server.api.model.CityBikeStatistic;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.gbfs.GbfsService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class JourneyBikeSummaryService {

    private final GbfsService gbfsService;

    @Builder
    @Data
    private static class StationDistance {
        private StationInformationV23DataStationsInner stationInformation;
        private StationStatusV23DataStationsInner stationStatus;
        private Double distance;
    }

    public CityBike getWarsawVeturiloStops(String regionName, Double lon, Double lat) {
        Map<String, List<StationStatusV23DataStationsInner>> stationStatusesDictionary = this.gbfsService.v2RegionalStationStatusPublic(regionName).getData().getStations().stream()
                .collect(Collectors.groupingBy(StationStatusV23DataStationsInner::getStationId));

        List<StationDistance> stationInformation = this.gbfsService.v2RegionalStationInformationPublic(regionName).getData().getStations().stream()
                .map(station -> StationDistance.builder()
                        .stationInformation(station)
                        .stationStatus(stationStatusesDictionary.get(station.getStationId()).get(0))
                        .distance(org.apache.lucene.util.SloppyMath.haversinMeters(station.getLat().doubleValue(), station.getLon().doubleValue(), lat, lon))
                        .build())
                .sorted(Comparator.comparing(StationDistance::getDistance))
                .filter(station -> station.getDistance() <= 1_500)
                .toList();

        String nearestName = stationInformation.stream()
                .map(StationDistance::getStationInformation)
                .map(StationInformationV23DataStationsInner::getName)
                .findFirst()
                .orElse("");

        int nearestDistance = stationInformation.stream()
                .map(StationDistance::getDistance)
                .map(Double::intValue)
                .findFirst()
                .orElse(0);

        return new CityBike()
                .nearestName(nearestName)
                .nearestDistance(nearestDistance)
                .nearest(stationInformation(stationInformation.stream().limit(1).toList()))
                ._100m(stationInformation(stationInformation, 100))
                ._250m(stationInformation(stationInformation, 250))
                ._500m(stationInformation(stationInformation, 500));
    }

    private CityBikeStatistic stationInformation(List<StationDistance> stationInformation, double distance) {
        List<StationDistance> stations = stationInformation.stream()
                .filter(station -> station.getDistance() <= distance)
                .toList();

        return stationInformation(stations);
    }

    private CityBikeStatistic stationInformation(List<StationDistance> stationInformation) {

        int numBikesAvailable = stationInformation.stream()
                .map(StationDistance::getStationStatus)
                .map(StationStatusV23DataStationsInner::getNumBikesAvailable)
                .reduce(0, Integer::sum);

        int numDocksAvailable = stationInformation.stream()
                .map(StationDistance::getStationStatus)
                .map(StationStatusV23DataStationsInner::getNumDocksAvailable)
                .reduce(0, Integer::sum);

        int capacity = stationInformation.stream()
                .map(StationDistance::getStationInformation)
                .map(StationInformationV23DataStationsInner::getCapacity)
                .reduce(0, Integer::sum);

        float occupancy = ((float) 100 * numBikesAvailable) / ((float) capacity);

        return new CityBikeStatistic()
                .numBikesAvailable(numBikesAvailable)
                .numDocksAvailable(numDocksAvailable)
                .numDocks(capacity)
                .occupancyRatio(occupancy);
    }
}
