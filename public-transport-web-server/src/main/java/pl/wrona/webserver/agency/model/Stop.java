package pl.wrona.webserver.agency.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stop_id_seq")
    @SequenceGenerator(name = "stop_id_seq", sequenceName = "stop_id_seq",  allocationSize=1)
    private Long osmId;

    private String name;
    private String ref;
    private double lon;
    private double lat;
}
