package pl.wrona.webserver.stops;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusStop {

    @Id
    private Long osmId;
    private String name;
    private String ref;
    private float lon;
    private float lat;
}

