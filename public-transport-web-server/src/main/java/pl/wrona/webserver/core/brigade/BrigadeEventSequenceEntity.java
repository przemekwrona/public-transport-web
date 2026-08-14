package pl.wrona.webserver.core.brigade;

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
@Table(name = "brigade_event_sequence")
public class BrigadeEventSequenceEntity {

    @EmbeddedId
    private BrigadeEventSequenceId id;

    @Column(name = "next_value", nullable = false)
    private Long nextValue;

}
