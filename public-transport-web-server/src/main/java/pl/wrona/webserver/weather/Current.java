package pl.wrona.webserver.weather;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Current(
        @JsonProperty("last_updated_epoch") int lastUpdatedEpoch,
        @JsonProperty("last_updated") String lastUpdated,

        @JsonProperty("temp_c") float tempC,
        @JsonProperty("temp_f") float tempF,
        @JsonProperty("is_day") int isDay,

        Condition condition,

        @JsonProperty("wind_mph") float windMph,
        @JsonProperty("wind_kph") float windKph,
        @JsonProperty("wind_degree") float windDegree,
        @JsonProperty("wind_dir") String windDir,

        @JsonProperty("pressure_mb") float pressureMb,
        @JsonProperty("pressure_in") float pressureIn,

        @JsonProperty("precip_mm") float precipMm,
        @JsonProperty("precip_in") float precipIn,

        int humidity,
        int cloud,

        @JsonProperty("feelslike_c") float feelslikeC,
        @JsonProperty("feelslike_f") float feelslikeF,

        @JsonProperty("windchill_c") float windchillC,
        @JsonProperty("windchill_f") float windchillF,

        @JsonProperty("heatindex_c") float heatindexC,
        @JsonProperty("heatindex_f") float heatindexF,

        @JsonProperty("dewpoint_c") float dewpointC,
        @JsonProperty("dewpoint_f") float dewpointF,

        @JsonProperty("vis_km") float visKm,
        @JsonProperty("vis_miles") float visMiles,
        float uv,
        @JsonProperty("gust_mph") float gustMph,
        @JsonProperty("gust_kph") float gustKph) {
}
