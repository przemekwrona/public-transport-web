package pl.wrona.webserver.stops;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stop")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusStop {

    @Id
    @Column(name = "stop_id")
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
}

