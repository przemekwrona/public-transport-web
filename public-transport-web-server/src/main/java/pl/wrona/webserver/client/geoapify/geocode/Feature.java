package pl.wrona.webserver.client.geoapify.geocode;

import java.util.List;

public record Feature(
        String type,
        Geometry geometry,
        Properties properties,
        List<Double> bbox
) {}

