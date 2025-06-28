package pl.wrona.webserver.client.geoapify.geocode;

import java.util.List;

public record Geometry(
        String type,
        List<Double> coordinates
) {}

