package pl.wrona.webserver.client.geoapify.geocode;

public record ParsedQuery(
        String housenumber,
        String street,
        String postcode,
        String city,
        String country,
        String expected_type
) {}
