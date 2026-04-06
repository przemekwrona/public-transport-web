package pl.wrona.webserver.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wrona.webserver.stops.BusStop;

import java.util.Set;

@Entity
@Data
@Builder
@Table(name = "territorial_unit")
@NoArgsConstructor
@AllArgsConstructor
public class TerritorialUnitEntity {

    @Id
    @Column(name = "territorial_unit_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "territorial_unit_id_seq")
    @SequenceGenerator(name = "territorial_unit_id_seq", sequenceName = "territorial_unit_id_seq", allocationSize = 1)
    private Long territorialUnitId;

    @Column(name = "lokalnyid")
    private String lokalnyid;

    @Column(name = "teryt")
    private String teryt;

    @Column(name = "idterc")
    private String idterc;

    @Column(name = "idsimc")
    private String idsimc;

    @Column(name = "idprng")
    private String idprng;

    @Column(name = "nazwa")
    private String nazwa;

    @Column(name = "rodzaj")
    private String rodzaj;

    @Column(name = "licz_miesz")
    private Integer liczMiesz;

    @Column(name = "lon")
    private String centroidLon;

    @Column(name = "lat")
    private String centroidLat;

    @OneToMany(mappedBy = "territorialUnit")
    private Set<BusStop> busStops;
}
