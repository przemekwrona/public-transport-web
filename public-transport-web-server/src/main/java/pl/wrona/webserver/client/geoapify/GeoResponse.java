package pl.wrona.webserver.client.geoapify;

import java.util.List;

public record GeoResponse(
        String type,
        List<Feature> features,
        Query query
) {}

