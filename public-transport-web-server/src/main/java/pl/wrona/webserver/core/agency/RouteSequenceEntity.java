package pl.wrona.webserver.core.agency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "route_sequence")
public class RouteSequenceEntity {

    @Id
    @Column(name = "agency_code", length = 15, nullable = false)
    private String agencyCode;

    @Column(name = "next_value", nullable = false)
    private Long nextValue;

}
