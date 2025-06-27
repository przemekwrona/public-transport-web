package pl.wrona.webserver.client.geoapify;

import java.util.List;

public record Geometry(
        String type,
        List<Double> coordinates
) {}

