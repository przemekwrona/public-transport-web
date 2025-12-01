package pl.wrona.webserver.bussiness.trip.measure;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.util.SloppyMath;
import org.igeolab.iot.pt.server.api.model.Point2D;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.igeolab.iot.pt.server.api.model.TripMeasure;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.client.geoapify.GeoapifyService;
import pl.wrona.webserver.client.geoapify.routing.Feature;
import pl.wrona.webserver.client.geoapify.routing.Geometry;
import pl.wrona.webserver.client.geoapify.routing.Leg;
import pl.wrona.webserver.client.geoapify.routing.Properties;
import pl.wrona.webserver.client.geoapify.routing.RoutingResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TripDistanceMeasureService {

    private final GeoapifyService geoapifyService;

    public TripMeasure approximateDistance(TripMeasure tripMeasure) {
        int meters = 0;
        int seconds = 0;

        List<StopTime> stopTimes = new ArrayList<>();

        for (Pair<StopTime, StopTime> pairStopTime : pairConsecutiveElements(tripMeasure.getStops())) {
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

                int defaultVelocityKph = 30;
                int velocityKph = Optional.ofNullable(tripMeasure.getVelocity()).orElse(defaultVelocityKph);

                // Convert km/h to m/s using a clear constant
                final double METERS_PER_SECOND_PER_KMPH = 1000.0 / 3600.0;
                double velocityMps = velocityKph * METERS_PER_SECOND_PER_KMPH;

                // Compute total seconds (rounded up to nearest minute)
                int travelTimeInSeconds = (int) Math.ceil(Math.round(haversinMeters / velocityMps) / 60.0d) * 60;
                seconds += travelTimeInSeconds;

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

        return new TripMeasure()
                .tripId(new TripId()
                        .routeId(new RouteId()
                                .line(tripMeasure.getTripId().getRouteId().getLine())
                                .name(tripMeasure.getTripId().getRouteId().getName()))
                        .variantMode(tripMeasure.getTripId().getVariantMode())
                        .trafficMode(tripMeasure.getTripId().getTrafficMode()))
                .stops(stopTimes)
                .distanceInMeters(meters)
                .travelTimeInSeconds(seconds)
                .velocity(tripMeasure.getVelocity())
                .geometry(geometry);
    }

    public TripMeasure measureDistance(TripMeasure tripMeasure) {
        String waypoints = tripMeasure.getStops().stream()
                .map(stop -> "%s,%s".formatted(stop.getLat(), stop.getLon()))
                .collect(Collectors.joining("|"));

        RoutingResponse routing = geoapifyService.route(waypoints, tripMeasure.getVelocity());
        Feature feature = routing.features().stream().findFirst().orElse(null);
        List<Leg> legs = Optional.ofNullable(feature).map(Feature::properties).map(Properties::legs).orElse(List.of());

        List<StopTime> stopTimes = new ArrayList<>(tripMeasure.getStops().size());

        double meters = 0;
        double seconds = 0;

        for (int i = 0; i < tripMeasure.getStops().size(); i++) {
            StopTime stopTime = tripMeasure.getStops().get(i);

            stopTimes.add(new StopTime()
                    .stopId(stopTime.getStopId())
                    .stopName(stopTime.getStopName())
                    .lon(stopTime.getLon())
                    .lat(stopTime.getLat())
                    .meters((int) meters)
                    .calculatedSeconds((int) seconds));

            if (i < legs.size()) {
                Leg leg = legs.get(i);

                meters += leg.distance();

                int travelTimeInSeconds = (int) Math.ceil(Math.round(leg.time()) / 60.0d) * 60;
                seconds += travelTimeInSeconds;
            }
        }

        List<Point2D> geometry = Optional.ofNullable(feature)
                .map(Feature::geometry)
                .map(Geometry::coordinates)
                .orElse(Collections.emptyList()).stream()
                .flatMap(Collection::stream)
                .map((List<Double> cords) -> new Point2D()
                        .lon(cords.get(0).floatValue())
                        .lat(cords.get(1).floatValue()))
                .toList();

        return new TripMeasure()
                .tripId(new TripId()
                        .routeId(new RouteId()
                                .line(tripMeasure.getTripId().getRouteId().getLine())
                                .name(tripMeasure.getTripId().getRouteId().getName()))
                        .variantName(tripMeasure.getTripId().getVariantName())
                        .trafficMode(tripMeasure.getTripId().getTrafficMode()))
                .stops(stopTimes)
                .distanceInMeters((int) meters)
                .travelTimeInSeconds((int) seconds)
                .velocity(tripMeasure.getVelocity())
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
