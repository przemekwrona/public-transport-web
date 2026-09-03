package pl.wrona.webserver.core.brigade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "brigade_resource", uniqueConstraints = {
        @UniqueConstraint(name = "uq_brigade_resource_group_sequence",
                columnNames = {"brigade_group_id", "resource_sequence"})
})
public class BrigadeResourceEntity {

    @Id
    @Column(name = "brigade_resource_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "brigade_resource_id_seq")
    @SequenceGenerator(name = "brigade_resource_id_seq", sequenceName = "brigade_resource_id_seq", allocationSize = 1)
    private Long brigadeResourceId;

    @ManyToOne
    @JoinColumn(name = "brigade_group_id", nullable = false)
    private BrigadeGroupEntity brigadeGroup;

    @Column(name = "resource_sequence", nullable = false)
    private Integer resourceSequence;

    @Column(name = "resource_code", nullable = false, length = 4)
    private String resourceCode;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

}
