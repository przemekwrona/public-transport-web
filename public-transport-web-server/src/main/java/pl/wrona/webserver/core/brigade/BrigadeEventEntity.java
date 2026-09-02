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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wrona.webserver.core.agency.TripProfileEntity;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "brigade_event")
public class BrigadeEventEntity {

    @Id
    @Column(name = "brigade_event_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "brigade_event_id_seq")
    @SequenceGenerator(name = "brigade_event_id_seq", sequenceName = "brigade_event_id_seq", allocationSize = 1)
    private Long brigadeEventId;

    @Column(name = "start_second", nullable = false)
    private Integer startSecond;

    @Column(name = "end_second", nullable = false)
    private Integer endSecond;

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private BrigadeResourceEntity resource;

    @ManyToOne
    @JoinColumn(name = "trip_profile_id", referencedColumnName = "trip_profile_id", nullable = false)
    private TripProfileEntity tripProfile;

    @Column(nullable = false, length = 10)
    private String line;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private Integer sequence;

    @Column(name = "sequence_hex", nullable = false, length = 4)
    private String sequenceHex;

}
