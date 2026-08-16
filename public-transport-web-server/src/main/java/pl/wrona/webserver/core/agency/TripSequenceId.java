package pl.wrona.webserver.core.agency;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class TripSequenceId implements Serializable {

    @Column(name = "agency_code", length = 15, nullable = false)
    private String agencyCode;

    @Column(name = "route_sequence", nullable = false)
    private Integer routeSequence;

}
