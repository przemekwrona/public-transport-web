package pl.wrona.webserver.client.geoapify.routing;

public record Feature(
        String type,
        Properties properties,
        Geometry geometry
) {}
