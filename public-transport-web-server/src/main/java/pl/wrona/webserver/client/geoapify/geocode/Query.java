package pl.wrona.webserver.client.geoapify.geocode;

public record Query(
        String text,
        ParsedQuery parsed
) {}
