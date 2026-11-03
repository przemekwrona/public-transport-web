package pl.wrona.webserver.core.agency;

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
import pl.wrona.webserver.core.entity.StopEntity;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stop_agency_history", uniqueConstraints = {
        @UniqueConstraint(name = "uq_stop_agency_history_agency_stop_valid_from",
                columnNames = {"agency_id", "stop_id", "valid_from"})
})
public class StopAgencyHistoryEntity {

    @Id
    @Column(name = "stop_agency_history_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stop_agency_history_id_seq")
    @SequenceGenerator(name = "stop_agency_history_id_seq", sequenceName = "stop_agency_history_id_seq", allocationSize = 1)
    private Long stopAgencyHistoryId;

    @ManyToOne
    @JoinColumn(name = "agency_id", nullable = false)
    private AgencyEntity agency;

    @ManyToOne
    @JoinColumn(name = "stop_id", nullable = false)
    private StopEntity stop;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private double lon;

    @Column(nullable = false)
    private double lat;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

}
