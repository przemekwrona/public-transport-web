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
import pl.wrona.webserver.core.brigade.BrigadeTripEntity;

import java.util.Set;

@Data
@Entity
@Table(name = "trip")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripEntity {

    @Id
    @Column(name = "trip_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trip_id_seq")
    @SequenceGenerator(name = "trip_id_seq", sequenceName = "trip_id_seq", allocationSize = 1)
    private Long tripId;

    @Column(name = "variant_name")
    private String variantName;

    @Enumerated(EnumType.STRING)
    private TripVariantMode mode;

    @Enumerated(EnumType.STRING)
    private TripTrafficMode trafficMode;

    private String headsign;

    private int communicationVelocity;

    private int distanceInMeters;

    private int travelTimeInSeconds;

    @Column(name = "is_main_variant")
    private boolean mainVariant;

    private String variantDesignation;

    private String variantDescription;

    @Column(name = "origin_stop_id")
    private String originStopId;

    @Column(name = "origin_stop_name")
    private String originStopName;

    @Column(name = "destination_stop_id")
    private String destinationStopId;

    @Column(name = "destination_stop_name")
    private String destinationStopName;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.REMOVE)
    private Set<StopTimeEntity> stopTimes;

    @OneToMany(mappedBy = "rootTrip", cascade = CascadeType.REMOVE)
    private Set<BrigadeTripEntity> brigadeTrips;

    @ManyToOne
    @JoinColumn(name = "route_id", referencedColumnName = "route_id")
    private RouteEntity route;

    @Column(name = "geometry")
    private String geometry;

}
