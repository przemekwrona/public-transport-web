package pl.wrona.webserver.agency.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Trip {

    @EmbeddedId
    private TripId tripId;

//    @ManyToOne
//    @MapsId("agencyCode")
//    @JoinColumn(name = "route_id", referencedColumnName = "route_id")
//    @JoinColumns({
//            @JoinColumn(name = "agency_code", referencedColumnName = "agency_code"),
//            @JoinColumn(name = "route_id", referencedColumnName = "route_id")
//    })
//    private Route route;

}
