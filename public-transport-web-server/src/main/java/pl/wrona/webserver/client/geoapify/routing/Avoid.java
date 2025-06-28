package pl.wrona.webserver.client.geoapify.routing;

import java.util.List;

public record Avoid(
        String type,
        List<LatLon> values
) {}
