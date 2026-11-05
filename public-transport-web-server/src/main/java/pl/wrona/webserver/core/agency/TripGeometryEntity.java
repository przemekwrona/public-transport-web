package pl.wrona.webserver.core.agency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@Table(name = "trip_geometry")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripGeometryEntity {

    @Id
    @Column(name = "trip_id")
    private Long tripId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "trip_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TripEntity trip;

    @Column(name = "geometry")
    private String geometry;

}
