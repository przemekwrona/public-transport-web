package pl.wrona.webserver.agency.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trip {

    @Id
    private String tripId;

    @ManyToOne
    @JoinColumn(name = "route_id", referencedColumnName = "route_id")
    private Route route;

}
