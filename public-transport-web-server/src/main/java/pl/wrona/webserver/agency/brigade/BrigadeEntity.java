package pl.wrona.webserver.agency.brigade;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeTrip;
import pl.wrona.webserver.agency.entity.Agency;

import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "brigade")
public class BrigadeEntity {

    @Id
    @Column(name = "brigade_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "brigade_id_generator")
    @SequenceGenerator(name = "brigade_id_generator", sequenceName = "brigade_id_seq",  allocationSize=1)
    private Long brigadeId;

    @ManyToOne
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    private String brigadeNumber;

    @OneToMany(mappedBy = "brigade", cascade = CascadeType.ALL)
    private Set<BrigadeTripEntity> brigadeTrips;
}
