package pl.wrona.webserver.client.geoapify.geocode;

public record Datasource(
        String sourcename,
        String attribution,
        String license,
        String url
) {}

