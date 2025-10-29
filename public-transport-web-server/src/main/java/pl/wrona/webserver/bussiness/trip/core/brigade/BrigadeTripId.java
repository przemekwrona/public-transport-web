package pl.wrona.webserver.bussiness.trip.core.brigade;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.igeolab.iot.pt.server.api.model.TripMode;

import java.io.Serializable;

@Data
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class BrigadeTripId implements Serializable {

    @Column(name = "line")
    private String line;

    @Column(name = "name")
    private String name;

    @Column(name = "variant")
    private String variant;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "mode")
    private TripMode mode;

    @Column(name = "trip_sequence")
    private int tripSequence;

}
