package pl.wrona.osm.stop.deactivate.stop;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@Table(name = "stop")
@NoArgsConstructor
@AllArgsConstructor
public class StopEntity {

    @Id
    @Column(name = "stop_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stop_id_seq")
    @SequenceGenerator(name = "stop_id_seq", sequenceName = "stop_id_seq", allocationSize = 1)
    private Long stopId;

    @Column(name = "bdot10k_id")
    private String bdot10kId;

    @Column(name = "osm_id")
    private String osmId;

    private String name;
    private double lon;
    private double lat;

    @Column(name = "is_bdot10k")
    private boolean bdot10k;

    @Column(name = "is_osm")
    private boolean osm;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "is_deactivation_checked")
    private boolean deactivatedChecked;

}
