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
@Table(name = "brigade_resource_sequences")
public class BrigadeResourceSequenceEntity {

    @EmbeddedId
    private BrigadeResourceSequenceId id;

    @Column(name = "next_value", nullable = false)
    private Integer nextValue;

}
