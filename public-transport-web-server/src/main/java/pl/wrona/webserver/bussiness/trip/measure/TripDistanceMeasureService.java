package pl.wrona.webserver.bussiness.trip.measure;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.util.SloppyMath;
import org.igeolab.iot.pt.server.api.model.Point2D;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.client.geoapify.GeoapifyService;
import pl.wrona.webserver.client.geoapify.routing.Feature;
import pl.wrona.webserver.client.geoapify.routing.Leg;
import pl.wrona.webserver.client.geoapify.routing.Properties;
import pl.wrona.webserver.client.geoapify.routing.RoutingResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TripDistanceMeasureService {

    private final GeoapifyService geoapifyService;

    public Trip approximateDistance(Trip trips) {
        int meters = 0;
        int seconds = 0;

        List<StopTime> stopTimes = new ArrayList<>();

        for (Pair<StopTime, StopTime> pairStopTime : pairConsecutiveElements(trips.getStops())) {
            if (pairStopTime.getLeft() == null) {
                stopTimes.add(new StopTime()
                        .stopId(pairStopTime.getRight().getStopId())
                        .stopName(pairStopTime.getRight().getStopName())
                        .lon(pairStopTime.getRight().getLon())
                        .lat(pairStopTime.getRight().getLat())
                        .meters(0)
                        .calculatedSeconds(0));
            } else {
                int haversinMeters = (int) SloppyMath.haversinMeters(pairStopTime.getLeft().getLat(),
                        pairStopTime.getLeft().getLon(),
                        pairStopTime.getRight().getLat(),
                        pairStopTime.getRight().getLon());

                meters = meters + haversinMeters;

                // Communication speed 45km/h
                int velocityKmPerH = Optional.ofNullable(trips.getCalculatedCommunicationVelocity()).orElse(45);
                double velocityMetersPerSec = (velocityKmPerH * 1000.0) / 3600.0d;
                seconds = seconds + (int) (((double) haversinMeters) / velocityMetersPerSec);

                stopTimes.add(new StopTime()
                        .stopId(pairStopTime.getRight().getStopId())
                        .stopName(pairStopTime.getRight().getStopName())
                        .lon(pairStopTime.getRight().getLon())
                        .lat(pairStopTime.getRight().getLat())
                        .meters(meters)
                        .calculatedSeconds(seconds));
            }

        }

        List<Point2D> geometry = stopTimes.stream()
                .map(stopTime -> new Point2D()
                        .lat(stopTime.getLat())
                        .lon(stopTime.getLon()))
                .toList();

        return new Trip()
                .line(trips.getLine())
                .variant(trips.getVariant())
                .headsign(trips.getHeadsign())
                .stops(stopTimes)
                .geometry(geometry);
    }

    public Trip measureDistance(Trip trips) {
        String waypoints = trips.getStops().stream()
                .map(stop -> "%s,%s".formatted(stop.getLat(), stop.getLon()))
                .collect(Collectors.joining("|"));

        RoutingResponse routing = geoapifyService.route(waypoints, trips.getCalculatedCommunicationVelocity());
        Feature feature = routing.features().stream().findFirst().orElse(null);
        List<Leg> legs = Optional.ofNullable(feature).map(Feature::properties).map(Properties::legs).orElse(List.of());

        List<StopTime> stopTimes = new ArrayList<>(trips.getStops().size());

        double meters = 0;
        double seconds = 0;

        for (int i = 0; i < trips.getStops().size(); i++) {
            StopTime stopTime = trips.getStops().get(i);

            stopTimes.add(new StopTime()
                    .stopId(stopTime.getStopId())
                    .stopName(stopTime.getStopName())
                    .lon(stopTime.getLon())
                    .lat(stopTime.getLat())
                    .meters((int) meters)
                    .calculatedSeconds((int) seconds)
            );

            if (i < legs.size()) {
                Leg leg = legs.get(i);

                meters += leg.distance();
                seconds += leg.time();
            }

        }

        List<Point2D> geometry = feature.geometry().coordinates().stream()
                .flatMap(Collection::stream)
                .map((List<Double> cords) -> new Point2D()
                        .lon(cords.get(0).floatValue())
                        .lat(cords.get(1).floatValue()))
                .toList();

        return new Trip()
                .line(trips.getLine())
                .variant(trips.getVariant())
                .headsign(trips.getHeadsign())
                .stops(stopTimes)
                .geometry(geometry);
    }

    private static List<Pair<StopTime, StopTime>> pairConsecutiveElements(List<StopTime> elements) {
        List<Pair<StopTime, StopTime>> pairedElements = new ArrayList<>();
        if (elements.isEmpty()) {
            return List.of();
        }
        pairedElements.add(Pair.of(null, elements.get(0)));

        for (int i = 0; i < elements.size() - 1; i++) {
            pairedElements.add(Pair.of(elements.get(i), elements.get(i + 1)));
        }
        return pairedElements;
    }


}
