package pl.wrona.webserver.core.agency;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Entity
@Table(name = "trip_profile")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripProfileEntity {

    @Id
    @Column(name = "trip_profile_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trip_profile_id_seq")
    @SequenceGenerator(name = "trip_profile_id_seq", sequenceName = "trip_profile_id_seq", allocationSize = 1)
    private Long tripProfileId;

    @ManyToOne
    @JoinColumn(name = "trip_id", referencedColumnName = "trip_id", nullable = false)
    private TripEntity trip;

    @Enumerated(EnumType.STRING)
    @Column(name = "traffic_mode", nullable = false)
    private TripTrafficMode trafficMode;

    private int travelTimeInSeconds;

    private int calculatedCommunicationVelocity;

    private int customizedCommunicationVelocity;

    @Column(name = "is_default")
    private boolean defaultProfile;

    @Column(name = "is_customized")
    private boolean customized;

    @OneToMany(mappedBy = "tripProfile", cascade = CascadeType.REMOVE)
    private Set<StopTimeEntity> stopTimes;

}
