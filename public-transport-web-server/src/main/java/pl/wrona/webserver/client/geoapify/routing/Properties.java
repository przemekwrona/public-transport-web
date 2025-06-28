package pl.wrona.webserver.client.geoapify.routing;

import java.util.List;

public record Properties(
        String mode,
        List<Waypoint> waypoints,
        String units,
        List<Avoid> avoid,
        double distance,
        String distance_units,
        double time,
        List<Leg> legs
) {}
