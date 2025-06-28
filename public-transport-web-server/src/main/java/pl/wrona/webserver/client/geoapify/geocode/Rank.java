package pl.wrona.webserver.client.geoapify.geocode;

public record Rank(
        double popularity,
        double confidence,
        Double confidence_city_level,
        Double confidence_street_level,
        Double confidence_building_level,
        String match_type,
        Double importance
) {}

