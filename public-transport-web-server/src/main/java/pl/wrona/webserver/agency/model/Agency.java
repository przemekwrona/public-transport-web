package pl.wrona.webserver.agency.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Agency {

    @Id
    private Long agencyId;

    @Column(name="agency_code", unique = true)
    private String agencyCode;

    @OneToMany(mappedBy = "agency", cascade = CascadeType.ALL)
    private Set<Route> routes;
}
