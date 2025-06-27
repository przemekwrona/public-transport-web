package pl.wrona.webserver.client.geoapify;

public record ParsedQuery(
        String housenumber,
        String street,
        String postcode,
        String city,
        String country,
        String expected_type
) {}
