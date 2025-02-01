package pl.wrona.webserver.agency.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stop {

    @Id
    @Column(name = "bdot10k_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stop_id_seq")
    @SequenceGenerator(name = "stop_id_seq", sequenceName = "stop_id_seq", allocationSize = 1)
    private String stopId;

    private String name;
    private double lon;
    private double lat;

    @OneToMany(mappedBy = "stop")
    private List<StopTimeEntity> stopTimeEntities;
}
