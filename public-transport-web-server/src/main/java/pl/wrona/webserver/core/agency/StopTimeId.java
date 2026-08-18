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
public class StopTimeId implements Serializable {

    @Column(name = "trip_profile_id")
    private Long tripProfileId;

    @Column(name = "stop_sequence")
    private int stopSequence;

}
