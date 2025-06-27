package pl.wrona.webserver.client.geoapify;

public record Datasource(
        String sourcename,
        String attribution,
        String license,
        String url
) {}

