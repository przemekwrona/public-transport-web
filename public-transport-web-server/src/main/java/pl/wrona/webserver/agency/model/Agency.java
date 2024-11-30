package pl.wrona.webserver.agency.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Agency {

    @Id
    private String agencyCode;

//    @OneToMany(mappedBy = "routeId.agency", cascade = CascadeType.ALL)
//    private Set<Route> routes;
}
