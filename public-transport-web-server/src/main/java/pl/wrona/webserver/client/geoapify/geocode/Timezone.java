package pl.wrona.webserver.client.geoapify.geocode;

public record Timezone(
        String name,
        String offset_STD,
        int offset_STD_seconds,
        String offset_DST,
        int offset_DST_seconds,
        String abbreviation_STD,
        String abbreviation_DST
) {}

