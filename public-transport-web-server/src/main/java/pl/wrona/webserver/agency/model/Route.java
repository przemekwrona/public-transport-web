package pl.wrona.webserver.agency.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    @EmbeddedId
    private RouteId routeId;

//    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
//    private Set<Trip> trips;

}
