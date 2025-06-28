package pl.wrona.webserver.client.geoapify.routing;

import java.util.List;

public record Waypoint(
        List<Double> location,
        int original_index
) {}
