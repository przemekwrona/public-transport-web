package pl.wrona.webserver.weather;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Location(String name, String region, String country, float lat, float lon,
                       @JsonProperty("tz_id") String tzId,
                       @JsonProperty("localtime_epoch") int localtimeEpoch,
                       String localtime) {
}
