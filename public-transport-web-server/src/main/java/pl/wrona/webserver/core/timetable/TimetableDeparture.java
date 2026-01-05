package pl.wrona.webserver.core.timetable;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class TimetableDeparture {

    @JsonProperty("t")
    private LocalTime time;

    @JsonProperty("s")
    private String symbol;
}
