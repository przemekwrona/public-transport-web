package pl.wrona.webserver.agency.entity;

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

import java.util.Set;

@Data
@Entity
@Table(name = "route")
@NoArgsConstructor
@AllArgsConstructor
public class RouteEntity {
    @Id
    @Column(name = "route_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "route_id_seq")
    @SequenceGenerator(name = "route_id_seq", sequenceName = "route_id_seq",  allocationSize=1)
    private Long routeId;

    private String name;

    private String line;

    @Column(name = "origin_stop_id")
    private Long originStopId;

    @Column(name = "origin_stop_name")
    private String originStopName;

    @Column(name = "destination_stop_id")
    private Long destinationStopId;

    @Column(name = "destination_stop_name")
    private String destinationStopName;

    private boolean active;

    private boolean google;

    private String description;

    private String via;

    @ManyToOne
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TripEntity> tripEntities;

}
