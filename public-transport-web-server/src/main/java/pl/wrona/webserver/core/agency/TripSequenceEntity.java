package pl.wrona.webserver.core.agency;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trip_sequence")
public class TripSequenceEntity {

    @EmbeddedId
    private TripSequenceId id;

    @Column(name = "next_value", nullable = false)
    private Long nextValue;

}
