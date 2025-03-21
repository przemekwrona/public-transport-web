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
import org.igeolab.iot.pt.server.api.model.TripMode;
import pl.wrona.webserver.agency.entity.TripEntity;

import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "brigade_trip")
public class BrigadeTripEntity {

    @Id
    @Column(name = "brigade_trip_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "brigade_trip_id_generator")
    @SequenceGenerator(name = "brigade_trip_id_generator", sequenceName = "brigade_trip_id_seq",  allocationSize=1)
    private Long brigadeTripId;

    private String line;

    private String name;

    private String variant;

    private TripMode mode;

    @ManyToOne
    @JoinColumn(name = "root_trip_id", referencedColumnName = "trip_id", nullable = false)
    private TripEntity rootTrip;

    @ManyToOne
    @JoinColumn(name = "brigade_id", nullable = false)
    private BrigadeEntity brigade;

    @Column(name = "departure_time_in_second")
    private int departureTimeInSeconds;

    @Column(name = "variant_designation")
    private String variantDesignation;

    @Column(name = "variant_description")
    private String variantDescription;

//    private int arrivalTimeInSeconds;
//
//    private int departureTimeInSeconds;
//
//    private String sourceTripId;

}
