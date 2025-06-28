package pl.wrona.webserver.client.geoapify.routing;

import java.util.List;

public record Geometry(
        String type,
        List<List<List<Double>>> coordinates
) {}
