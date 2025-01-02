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
@Table(name = "bdot10k_stop")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusStop {

    @Id
    @Column(name = "bdot10k_id")
    private String bdot10kId;

    private String name;

    private float lon;

    private float lat;
}

