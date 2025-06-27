package pl.wrona.webserver.client.geoapify;

public record Query(
        String text,
        ParsedQuery parsed
) {}
