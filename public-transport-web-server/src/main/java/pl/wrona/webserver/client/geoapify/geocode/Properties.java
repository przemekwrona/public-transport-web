package pl.wrona.webserver.client.geoapify.geocode;

public record Properties(
        String country_code,
        String name,
        String country,
        String county,
        Datasource datasource,
        String state,
        String state_code,
        String district,
        String city,
        String suburb,
        String street,
        String housenumber,
        String postcode,
        double lon,
        double lat,
        String result_type,
        String formatted,
        String address_line1,
        String address_line2,
        String plus_code,
        String plus_code_short,
        String iso3166_2,
        String category,
        Timezone timezone,
        Rank rank,
        String place_id
) {}

