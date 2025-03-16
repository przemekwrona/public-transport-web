package pl.wrona.webserver.agency.brigade;

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
import pl.wrona.webserver.agency.entity.Agency;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "brigade")
public class BrigadeEntity {

    @Id
    @Column(name = "brigade_trip_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "brigade_trip_id_generator")
    @SequenceGenerator(name = "brigade_trip_id_generator", sequenceName = "brigade_trip_id_seq",  allocationSize=1)
    private Long brigadeTripId;

    @ManyToOne
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    private String brigadeNumber;
}
